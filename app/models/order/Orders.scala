package models.order

import models.basic.Tables
import models.DBAccess
import models.ObjectModel
import models.enums.OrderStatus._
import org.joda.time.DateTime
import shapeless._
import shapeless.HList._
import shapeless.Tuples._
import scalaz.std.option._
import org.joda.time.format.DateTimeFormat
import models.partdef.PartDefinitionForList
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

case class OrderDesc(name:String, fillingDate:DateTime, dueDate:Option[DateTime], defs:List[OrderDefinitionDesc])

case class OrderDefinitionDesc(id:Option[Int], partDefId:Int, filter:String, count:Int)

case class OrderForList(id:Int, name:String, fillingDate:DateTime, dueDate:Option[DateTime], status:OrderStatus) {
	def fillDateString = fillingDate.toString(DateTimeFormat.shortDate())
	def dueDateString = dueDate.map(_.toString(DateTimeFormat.shortDate())).getOrElse("")
}

case class OrderDefStatus(odefId:Int, parts:List[PartInOrder])

case class PartInOrder(basicShapeId:Int, materialId:Int, count:Int)

case class OrderDefForList(id:Int, orderName:String, partDefName:String, filter:String, count:Int) {
	def description = s"$orderName - $partDefName ($filter)"
}

trait Orders extends Tables {
    self:DBAccess =>

    import profile.simple._
      
    def getOrder(id:Int)(implicit session:Session) = {
        val q = for {
            ord <- Order
        } yield (ord.name, ord.fillingDate, ord.dueDate)
        val q2 = for {
            odef <- OrderDefinition if odef.orderId === id
        } yield (odef.id, odef.partDefId, odef.filter, odef.count)
        for {
            (name, fill, due) <- q.firstOption
        } yield OrderDesc(name, fill, due, q2.list.map{
        	case (id, pdef, filt, cnt) => OrderDefinitionDesc(Some(id), pdef, filt, cnt)
        })
    }

    def existsOrderDefinition(id:Int)(implicit session:Session) =
        Query(OrderDefinition).filter(_.id === id).firstOption.isDefined

    def insertOrder(ord:OrderDesc)(implicit session:Session) = {
        val id:Int = Order.forInsert.insert((ord.name, ord.fillingDate, ord.dueDate, Accepted))
        OrderDefinition.forInsert.insertAll(ord.defs.map(x=>(id, x.partDefId, x.count, x.filter)):_*) 
        id
    }
    
    def updateOrder(id:Int, ord:OrderDesc)(implicit session:Session) = {
    	val q = for {
    		dbo <- Order if dbo.id === id
    	} yield dbo.name ~ dbo.fillingDate ~ dbo.dueDate
    	q.update(ord.name, ord.fillingDate, ord.dueDate)
    	val toDeleteQ = for {
    		odef <- OrderDefinition
    		if odef.orderId === id
    		if !odef.id.inSet(ord.defs.map(_.id).collect{case Some(id) => id})
    	} yield odef
    	val toDeleteIds = toDeleteQ.map(_.id).list
    	Query(Part).filter(_.orderDefId.inSet(toDeleteIds)).map(_.orderDefId).update(None)
    	toDeleteQ.delete
    	for (odef <- ord.defs) {
    		odef.id match {
    			case Some(odefId) => Query(OrderDefinition).filter(_.orderId === id).
    					filter(_.id === odefId).map(x=>x.filter ~ x.count).
    					update(odef.filter, odef.count)
    			case None => OrderDefinition.
    			        forInsert.insert((id, odef.partDefId, odef.count, odef.filter))
    		}
    	}
    }
    
    def updateOrderStatus(id:Int, statuses:List[OrderDefStatus])(implicit s:Session) {
    	val defs = Query(OrderDefinition).filter(_.orderId === id).map(_.id).list
        val nonFreeParts = for {
            p <- Part
            c <- Cutting if p.cuttingId === c.id && c.finishTime.isNotNull
        } yield p
        nonFreeParts.map(_.id).list.foreach(pid => Query(Part).filter(_.id === pid).map(_.orderDefId).update(None))
        val partCounts = Query(OrderDefinition).filter(_.orderId === id).map(x=>x.id -> x.count).list.toMap
        for{
            status <- statuses
            partStatus <- status.parts
        } {
            val have = Query(Query(Part).filter(_.orderDefId === status.odefId).length).first
            val toAdd = math.min(partCounts(status.odefId) - have, partStatus.count)
            val q = for {
                p <- Part if !p.damaged && p.orderDefId.isNull
                c <- Cutting if p.cuttingId === c.id && c.finishTime.isNotNull
                sp <- c.semiproduct
                pck <- sp.pack if pck.materialId === partStatus.materialId
                shp <- Shape if shp.id === pck.shapeId && shp.basicShapeId === partStatus.basicShapeId
            } yield p.id
            
            q.take(toAdd).list.foreach { id =>
                Query(Part).filter(_.id === id).map(_.orderDefId).update(some(status.odefId))
            }
        }
        tryFinishOrders(Query(Order).filter(_.id===id))
    }

    def orderStatus(id:Int)(implicit s:Session) = {
        sql"""
        select od.id, coalesce(fin.count, 0) as fin_count, fin.material_id, fin.basic_shape_id from "order" o 
          inner join order_def od on o.id = od.order_id 
          left join 
            (select count(p.id) as count, pck.material_id, shp.basic_shape_id, p.order_def_id from 
              part p 
              inner join cutting c on c.id = p.cutting_id
              inner join semiproduct sp on sp.id = c.semiproduct_id 
              inner join pack pck on pck.id = sp.pack_id 
              inner join shape shp on shp.id = pck.shape_id
            where c.finish_time is not null or c.id is null
            group by shp.basic_shape_id, pck.material_id, p.order_def_id) fin
          on fin.order_def_id = od.id
          where o.id=$id order by od.id""".as[(Int, Int, Option[Int], Option[Int])].list.groupBy(_._1).map {
            case (odid, lst) => OrderDefStatus(odid, lst.collect {
            	case (_, cnt, Some(mat), Some(shp)) if cnt > 0 => PartInOrder(shp, mat, cnt)
            })
        }.toList
    }

    def tryFinishOrders[E](orders:Query[Order.type, E])(implicit s:Session) {
        val partQ = Query(Part).filter(p => !Query(Cutting).filter(_.id === p.cuttingId).filter(_.finishTime.isNull).exists).
            groupBy(_.orderDefId).map(x=> x._1 -> x._2.length)
        val unFinished = Query(OrderDefinition).
            filter(odef => partQ.filter(_._1 === odef.id).filter(_._2 < odef.count).exists || !partQ.filter(_._1 === odef.id).exists)
        val toFinish = orders.filter(ord => !unFinished.filter(_.orderId === ord.id).exists).map(_.id)
        def chStatus(l:List[Int], status:OrderStatus) {
            l.foreach(id => Query(Order).filter(_.id === id).map(_.status).update(status))
        }
        chStatus(toFinish.list, Finished)
        chStatus(orders.filter(!_.id.in(toFinish)).map(_.id).list, Accepted)
    }
    
    def orderDefQuery = for {
    	odef <- OrderDefinition
    	ord <- Order if ord.id === odef.orderId
    	pdef <- PartDefinition if odef.partDefId === pdef.id
    } yield (odef, ord, pdef)
  
    def orderDefForListProjection(odef:OrderDefinition.type, ord:Order.type, pdef:PartDefinition.type) = 
    	(odef.id, ord.name, pdef.name, odef.filter, odef.count)
    
    def listOrderDefs(ordId:Int, cpId:Option[Int])(implicit s:Session) = {
        val q = cpId match {
            case None => orderDefQuery
            case Some(cp) => orderDefQuery.filter(odef => Query(PartDefinitionInCuttingPlan).
                                 filter(_.partDefId === odef._1.partDefId).filter(_.cutPlanId===cp).exists)
        }
    	q.filter(_._2.id === ordId).map((orderDefForListProjection _).tupled).
    	    list.map(OrderDefForList.tupled)
    }
    
    def orderDefDescription(id:Int)(implicit s:Session) = {
    	orderDefQuery.filter(_._1.id === id).map((orderDefForListProjection _).tupled).
    	    firstOption.map(OrderDefForList.tupled)
    }
  
}
