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

case class OrderDesc(name:String, fillingDate:DateTime, dueDate:Option[DateTime], defs:List[OrderDefinitionDesc])

case class OrderDefinitionDesc(id:Option[Int], partDefId:Int, filter:String, count:Int)

case class OrderForList(id:Int, name:String, fillingDate:DateTime, dueDate:Option[DateTime], status:OrderStatus) {
	def fillDateString = fillingDate.toString(DateTimeFormat.shortDate())
	def dueDateString = dueDate.map(_.toString(DateTimeFormat.shortDate())).getOrElse("")
}

case class OrderDefStatus(odefId:Int, parts:List[PartInOrder])

case class PartInOrder(partId:Int)

case class OrderDefForList(id:Int, partDefName:String, filter:String, count:Int)

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
        println(s"updateOrder $ord")
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
    
    def updateOrderStatus(id:Int, statuses:List[OrderDefStatus])(implicit s:Session) {
    	val defs = Query(OrderDefinition).filter(_.orderId === id).map(_.id).list
        Query(Part).filter(_.orderDefId.inSet(defs)).map(_.orderDefId).update(None)
        val partCounts = Query(OrderDefinition).filter(_.orderId === id).map(x=>x.id -> x.count).list.toMap
        for(status<-statuses) {
            val toAdd = math.min(partCounts(status.odefId), status.parts.size)
            for(part<-status.parts.take(toAdd)) {
                Query(Part).filter(_.id === part.partId).map(_.orderDefId).update(some(status.odefId))
            }
        }
        tryFinishOrders(Query(Order).filter(_.id===id))
    }

    def orderDefParts(orderDefId:Int)(implicit s:Session) = {
        val q = for {
            p <- Part
            c <- Cutting if p.cuttingId === c.id && p.orderDefId === some(orderDefId) && 
                c.finishTime.isNotNull && p.damaged === false
        } yield p.id
        q.list.map(PartInOrder)
    }

    def orderStatus(id:Int)(implicit s:Session) = {
        Query(OrderDefinition).filter(_.orderId === id).map(_.id).list.map(x=> x -> orderDefParts(x)).
            map(OrderDefStatus.tupled)
    }

    def tryFinishOrders[E](orders:Query[Order.type, E])(implicit s:Session) {
        val partQ = Query(Part).groupBy(_.orderDefId).map(x=> x._1 -> x._2.length)
        val unFinished = Query(OrderDefinition).
            filter(odef => partQ.filter(_._1 === odef.id).filter(_._2 < odef.count).exists || !partQ.filter(_._1 === odef.id).exists)
        val toFinish = orders.filter(ord => !unFinished.filter(_.orderId === ord.id).exists).map(_.id)
        def chStatus(l:List[Int], status:OrderStatus) {
            l.foreach(id => Query(Order).filter(_.id === id).map(_.status).update(status))
        }
        chStatus(toFinish.list, Finished)
        chStatus(orders.filter(!_.id.in(toFinish)).map(_.id).list, Accepted)
    }
  
    def listOrderDefs(ordId:Int)(implicit s:Session) = {
    	val q = for {
    		odef <- OrderDefinition if odef.orderId === ordId
    		pdef <- PartDefinition if odef.partDefId === pdef.id
    	} yield (odef.id, pdef.name, odef.filter, odef.count)
    	q.list.map(OrderDefForList.tupled)
    }
  
}
