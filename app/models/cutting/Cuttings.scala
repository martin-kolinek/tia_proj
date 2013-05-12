package models.cutting

import org.joda.time.DateTime
import models.DBAccess
import models.basic.Tables
import models.ObjectModel
import shapeless._
import shapeless.HList._
import shapeless.Tuples._
import models.cutplan.CuttingPlans
import models.cutplan.CuttingPlanForList
import models.semiproduct.PackForList
import models.semiproduct.SemiproductForList
import models.semiproduct.PackForList
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import models.order.Orders
import scalaz.std.option._
import org.joda.time.format.DateTimeFormat
import models.SlickExtensions._
import models.semiproduct.Semiproducts

case class CuttingDesc(semiprodId:Int, cutPlanId:Int, parts:List[PartInCuttingDesc])

case class PartInCuttingDesc(orderDefId:Int, count:Int)

case class FinishedPartInCutting(partDefId:Int, orderDefId:Option[Int], dmgCount:Int)

case class CuttingForList(id:Int, cutPlan:CuttingPlanForList, semiproductSerial:String, pack:PackForList, finishTime:Option[DateTime]) {
	def finished = finishTime.isDefined
	def finishTimeString = finishTime.map(_.toString(DateTimeFormat.shortDateTime()))
}

case class PartDesc(id:Int, partDefId:Int)

trait Cuttings extends CuttingPlans {
    self:DBAccess with Orders with Semiproducts => 

    import profile.simple._

    def getCutting(id:Int)(implicit s:Session) = {
        val cutting = Query(Cutting).filter(_.id === id).filter(_.finishTime.isNull).map(x=>(x.semiproductId, x.cuttingPlanId)).
            firstOption
        val parts = getPartCounts(id) 
        cutting.map(x=>(x.hlisted ::: parts :: HNil).tupled).map(CuttingDesc.tupled)
    }

    def getDamagedPartCounts(id:Int)(implicit session:Session) = {
    	sql"""
        SELECT deford.part_def_id, deford.order_def_id, coalesce(cnts.dmg, 0) FROM
          (SELECT DISTINCT part_def_id, order_def_id FROM part WHERE cutting_id=$id) deford
          left join
          (SELECT part_def_id, order_def_id, count(id) as dmg FROM part
                WHERE cutting_id=$id and damaged=true group by part_def_id, order_def_id, cutting_id) cnts
            on cnts.part_def_id = deford.part_def_id and
                (cnts.order_def_id = deford.order_def_id or cnts.order_def_id is null and deford.order_def_id is null)
          ORDER BY deford.part_def_id, deford.order_def_id
        """.as[(Int, Option[Int], Int)].list.map(FinishedPartInCutting.tupled)
    }
    
    private def getPartCounts(id:Int)(implicit session:Session) = 
    	Query(Part).filter(_.cuttingId === id).filter(_.orderDefId.isNotNull).
    	    groupBy(x=>(x.orderDefId, x.partDefId)).map{
    	        case ((odefid, pdefid), rows) => (odefid.get, rows.length)
    	    }.list.map(PartInCuttingDesc.tupled)

    def insertCutting(cut:CuttingDesc)(implicit s:Session) = {
        val cutid = Cutting.forInsert.insert(None, cut.semiprodId, cut.cutPlanId)
        val cp = getCuttingPlan(cut.cutPlanId)
        
        for(pdef <- cp.get.partdefs) {
        	Part.forInsert.insertAll((1 to pdef.count).map(x=>(None, pdef.partDefId, cutid, false)):_*)
        }
        
        updateCutting(cutid, cut)
        fixUnlimitedSemiproduct(cut.semiprodId)
        cutid
    }
    
    def countMapDefault(m:Map[Int, Map[Int, Int]]) = m.withDefault(x=>Map[Int, Int]().withDefault(y=>0))

    def adjustMap[A, B](m:Map[A, B], k:A)(f:B => B) = {
    	m.updated(k, f(m(k)))
    }
    
    def mapDiff (p1:Map[Int, Map[Int, Int]], p2:Map[Int, Map[Int, Int]]) = {
    	val withDef = p2.withDefault(x=>Map[Int, Int]().withDefault(x=>0))
    	for{
    		(pdef, map) <- p1 
    		resMap = for((ord, count) <- map) yield ord -> (count - withDef(pdef)(ord))
    	} yield pdef -> resMap
    }
    
    def updateCutting(id:Int, cut:CuttingDesc)(implicit s:Session) {
    	Query(Part).filter(_.cuttingId === id).map(_.orderDefId).update(None)
    	for(p <- cut.parts) {
    		updateCuttingParts(id, p.orderDefId, p.count)
    	} 
    }

    def updateCuttingParts(cuttingId:Int, orderDefId:Int, count:Int)(implicit s:Session) {
    	for {
    		(req, pdefId) <- Query(OrderDefinition).filter(_.id === orderDefId).map(x => x.count -> x.partDefId).firstOption
    		done <- Query(Query(Part).filter(_.orderDefId===some(orderDefId)).length).firstOption
    	} {
    		val take = math.min(req - done, count)
    		val toAdd = Query(Part).filter(_.damaged===false).filter(_.orderDefId.isNull).
    		    filter(_.cuttingId === cuttingId).
    		    filter(_.partDefId === pdefId).
    		    map(_.id).sortBy(identity).take(take).list
    		toAdd.foreach(pid => Query(Part).filter(_.id===pid).map(_.orderDefId).update(Some(orderDefId)))
    	}
    }

    def updateFinished(cutId:Int, fin:List[FinishedPartInCutting])(implicit s:Session) {
        for(p<-fin) {
            val ids = Query(Part).
            	filter(_.cuttingId === cutId).
                filter(if(p.orderDefId.isEmpty) x => x.orderDefId.isNull.? else x=>x.orderDefId === p.orderDefId).
                map(_.id).sortBy(identity).list
            val (dmg, ok) = ids.splitAt(p.dmgCount)
            dmg.foreach(id => Query(Part).filter(_.id === id).map(x=>x.orderDefId ~ x.damaged).update(None, true))
            ok.foreach(id => Query(Part).filter(_.id === id).map(x=>x.damaged).update(false))
        }
        val q = Query(Cutting).filter(_.id===cutId).map(_.finishTime)
        if(q.firstOption.map(_.isEmpty).getOrElse(false)) {
        	q.update(Some(DateTime.now()))
        }
        val ordDefs = fin.map(_.orderDefId).flatten
        val orders = Query(Order).
            filter(ord => Query(OrderDefinition).filter(_.orderId === ord.id).filter(_.id.inSet(ordDefs)).exists)
        tryFinishOrders(orders)
    }

    def listParts(cutId:Int)(implicit s:Session) = 
        Query(Part).filter(_.cuttingId === cutId).filter(_.damaged === false).map(x=>(x.id, x.partDefId)).list.map(PartDesc.tupled)
}
