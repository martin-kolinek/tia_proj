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

case class CuttingDesc(semiprodId:Int, cutPlanId:Int, parts:List[PartInCuttingDesc])

case class PartInCuttingDesc(partDefId:Int, orderId:Int, count:Int)

case class FinishedPartInCutting(partDefId:Int, orderId:Int, dmgCount:Int)

case class CuttingForList(id:Int, cutPlan:CuttingPlanForList, semiproduct:SemiproductForList, pack:PackForList)

trait Cuttings extends CuttingPlans {
    self:DBAccess =>

    import profile.simple._

    def getCutting(id:Int)(implicit s:Session) = {
        val cutting = Query(Cutting).filter(_.id === id).filter(_.finishTime.isNull).map(x=>(x.semiproductId, x.cuttingPlanId)).
            firstOption
        val parts = getPartCounts(id) 
        cutting.map(x=>(x.hlisted ::: parts :: HNil).tupled).map(CuttingDesc.tupled)
    }

    def getDamagedPartCounts(id:Int)(implicit session:Session) = 
        Query(Part).filter(_.cuttingId === id).groupBy(x=>x.partDefId -> x.orderId).map {
            case ((partDefId, orderId), rows) => (partDefId, orderId, rows.length)
        }.list.map(FinishedPartInCutting)
    
    private def getPartCounts(id:Int)(implicit session:Session) = 
    	Query(Part).filter(_.cuttingId === id).filter(_.orderId.isNotNull).groupBy(x=>x.partDefId -> x.orderId).map{
            case ((partDefId, orderId), rows) => (partDefId, orderId.get, rows.length)
        }.list.map(PartInCuttingDesc.tupled) 
    
    private def getPartDefOrderCounts(parts:Traversable[PartInCuttingDesc]) = 
    	parts.groupBy(x=>x.partDefId).map {
        	case (pdef, parts) => pdef -> parts.groupBy(x=>x.orderId).map {
        	    case (ord, parts) => ord -> parts.map(_.count).sum
        	}
        }

    def insertCutting(cut:CuttingDesc)(implicit s:Session) = {
        val cutid = Cutting.forInsert.insert(None, cut.semiprodId, cut.cutPlanId)
        val cp = getCuttingPlan(cut.cutPlanId)
        
        val partDefOrderCounts = countMapDefault(getPartDefOrderCounts(cut.parts))
        
        val partDefCounts = partDefOrderCounts.map {
        	case (pdef, ordCounts) => pdef -> ordCounts.map(_._2).sum
        }.withDefault(x=>0)
        
        for(pdef <- cp.get.partdefs) {
        	val rest = pdef.count - partDefCounts(pdef.partDefId)
        	assert(rest>=0)
        	val toInsWithOrder = for {
        		(order, count) <- partDefOrderCounts(pdef.partDefId).toSeq
        		i <- 1 to count
        	} yield (Some(order), pdef.partDefId, cutid, false)
        	Part.forInsert.insertAll(toInsWithOrder:_*)
        	val toInsWithoutOrder = for(i<- 1 to rest) 
        		yield (None, pdef.partDefId, cutid, false)
        	Part.forInsert.insertAll(toInsWithoutOrder:_*)
        }
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
        Cutting.filter(_.id === id).map(x=>x.semiproductId ~ x.cuttingPlanId).update(cut.semiprodId, cut.cutPlanId)
        val parts = Query(Part).filter(_.cuttingId === id).sortBy(_.partDefId).
            map(x=> x.id ~ x.partDefId ~ x.orderId).list
        val partDefOrderCounts = countMapDefault(mapDiff(getPartDefOrderCounts(cut.parts), getPartDefOrderCounts(getPartCounts(id))).
            map(x=> x._1 -> x._2.filter(y=>y._2 > 0)))
        (partDefOrderCounts /: parts){ (counts, part) =>
        	part match {
        		case (id, pdefid, None) => {
        			if(counts(pdefid).size!=0) {
        				val ord = counts(pdefid).head._1
        				Part.filter(_.id === id).map(_.orderId).update(Some(ord))
        				adjustMap(counts, pdefid) { ordCounts =>
        					if(ordCounts(ord) == 1) 
        						ordCounts - ord
        					else
        						adjustMap(ordCounts, ord)(_-1)
        				}
        				
        			} 
        			else
        				counts
        		}
        		case (id, pdefid, Some(ord)) =>  {
        			if(!counts(pdefid).isDefinedAt(ord)) {
        				Part.filter(_.id === id).map(_.orderId).update(None)
        			}
        			counts
        		}
        	}
        }
    }

    def updateFinished(cutId:Int, fin:List[FinishedPartInCutting])(implicit s:Session) {
        for(p<-fin) {
            val q = Query(Part).filter(if(p.orderId.isEmpty) x => x.isNull else x=>x===p.orderId)
        }
    }
}
