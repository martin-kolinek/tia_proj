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

case class OrderDesc(name:String, fillingDate:DateTime, dueDate:Option[DateTime], defs:List[OrderDefinitionDesc])

case class OrderDefinitionDesc(id:Option[Int], partDefId:Int, filter:String, count:Int)

case class OrderForList(id:Int, name:String, fillingDate:DateTime, dueDate:Option[DateTime], status:OrderStatus)

case class OrderDefStatus(odefId:Int, parts:List[PartInOrder])

case class PartInOrder(cuttingId:Int, count:Int)

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

    def existsOrder(id:Int)(implicit session:Session) =
        Query(Order).filter(_.id === id).firstOption.isDefined

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
    	(for {
    		odef <- OrderDefinition
    		if odef.orderId === id
    		if !odef.id.inSet(ord.defs.map(_.id).collect{case Some(id) => id})
    	} yield odef).delete
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

    def updateCutting(cuttingId:Int, orderDefId:Int, count:Int)(implicit s:Session) {
    	for {
    		req <- Query(OrderDefinition).filter(_.id === orderDefId).map(_.count).firstOption
    		done <- Query(Query(Part).filter(_.orderDefId===some(orderDefId)).length).firstOption
    	} {
    		val take = math.min(req - done, count)
    		val toAdd = Query(Part).filter(_.damaged===false).filter(_.orderDefId.isNull).filter(_.cuttingId === cuttingId).
    		map(_.id).sortBy(identity).take(take).list
    		toAdd.foreach(pid => Query(Part).filter(_.id===pid).map(_.orderDefId).update(Some(orderDefId)))
    	}
    }
    
    def updateOrderStatus(id:Int, statuses:List[OrderDefStatus])(implicit s:Session) {
    	val defs = Query(OrderDefinition).filter(_.orderId === id).map(_.id).list
        Query(Part).filter(_.orderDefId.inSet(defs)).map(_.orderDefId).update(None)
        for{
            status<-statuses
            part<-status.parts
        } {
            updateCutting(part.cuttingId, status.odefId, part.count)
        }
    }

    def orderDefParts(orderDefId:Int)(implicit s:Session) = 
        Query(Part).filter(_.orderDefId===some(orderDefId)).groupBy(_.cuttingId).map {
            case (cutId, rows) => (cutId, rows.length)
        }.list.map(PartInOrder.tupled)

    def orderStatus(id:Int)(implicit s:Session) = {
        Query(OrderDefinition).filter(_.orderId === id).map(_.id).list.map(x=> x -> orderDefParts(x)).
            map(OrderDefStatus.tupled)
    }
}
