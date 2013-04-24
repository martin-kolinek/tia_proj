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

case class OrderDesc(name:String, fillingDate:DateTime, dueDate:Option[DateTime], partdefs:List[PartDefInOrder])

case class PartDefInOrder(partDefId:Int, filter:String, count:Int)

case class OrderForList(id:Int, name:String, fillingDate:DateTime, dueDate:Option[DateTime], status:OrderStatus)

case class OrderDefStatus(partDefId:Int, parts:List[PartInOrder])

case class PartInOrder(cuttingId:Int, count:Int)

trait Orders extends Tables {
    self:DBAccess =>

    import profile.simple._
      
    def getOrder(id:Int)(implicit session:Session) = {
        val q = for {
            ord <- Order
        } yield (ord.name, ord.fillingDate, ord.dueDate)
        val q2 = for {
            pdef <- PartDefinitionInOrder if pdef.orderId === id
        } yield (pdef.partDefId, pdef.filter, pdef.count)
        for {
            ord <- q.firstOption
        } yield OrderDesc.tupled((ord.hlisted ::: (q2.list.map(PartDefInOrder.tupled) :: HNil)).tupled)
    }

    def existsOrder(id:Int)(implicit session:Session) =
        Query(Order).filter(_.id === id).firstOption.isDefined

    def insertOrder(ord:OrderDesc)(implicit session:Session) = {
        val id:Int = Order.forInsert.insert((ord.name, ord.fillingDate, ord.dueDate, Accepted))
        PartDefinitionInOrder.insertAll(ord.partdefs.map(x=>(id, x.partDefId, x.count, x.filter)):_*) 
        id
    }
    
    def updateOrder(id:Int, ord:OrderDesc)(implicit session:Session) = {
    	val q = for {
    		dbo <- Order if dbo.id === id
    	} yield dbo.name ~ dbo.fillingDate ~ dbo.dueDate
    	q.update(ord.name, ord.fillingDate, ord.dueDate)
    	(for {
    		pdef <- PartDefinitionInOrder
    		if pdef.orderId === id
    		if !pdef.partDefId.inSet(ord.partdefs.map(_.partDefId))
    	} yield pdef).delete
    	for (pdef <- ord.partdefs) {
    		val upd = Query(PartDefinitionInOrder).filter(_.orderId === id).
    		    filter(_.partDefId===pdef.partDefId).map(x=>x.filter ~ x.count).
    		    update(pdef.filter, pdef.count)
    		if(upd == 0) 
    			PartDefinitionInOrder.insert(id, pdef.partDefId, pdef.count, pdef.filter)
    	}
    }

    def updateOrderStatus(id:Int, statuses:List[OrderDefStatus])(implicit s:Session) {
        Query(Part).filter(_.orderId === some(id)).map(_.orderId).update(None)
        for{
            status<-statuses
            part<-status.parts
        } {
            for {
                req <- Query(PartDefinitionInOrder).filter(_.orderId === id).filter(_.partDefId === status.partDefId).map(_.count).firstOption
                done <- Query(Query(Part).filter(_.orderId===some(id)).filter(_.partDefId === status.partDefId).length).firstOption
            } {
                val take = math.min(req - done, part.count)
                val toAdd = Query(Part).filter(_.damaged===false).filter(_.orderId.isNull).filter(_.cuttingId===part.cuttingId).
                    map(_.id).sortBy(identity).take(take).list
                toAdd.foreach(pid => Query(Part).filter(_.id===pid).map(_.orderId).update(Some(id)))
            }
        }
    }

    def orderDefParts(orderId:Int, partDefId:Int)(implicit s:Session) = 
        Query(Part).filter(_.orderId===some(orderId)).filter(_.partDefId === partDefId).groupBy(_.cuttingId).map {
            case (cutId, rows) => (cutId, rows.length)
        }.list.map(PartInOrder.tupled)

    def orderStatus(id:Int)(implicit s:Session) = {
        Query(PartDefinitionInOrder).filter(_.orderId === id).map(_.partDefId).list.map(x=> x -> orderDefParts(id, x)).
            map(OrderDefStatus.tupled)
    }
}
