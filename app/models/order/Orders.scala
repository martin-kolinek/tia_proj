package models.order

import models.basic.Tables
import models.DBAccess
import models.ObjectModel
import models.enums.OrderStatus._
import org.joda.time.DateTime
import shapeless._
import shapeless.HList._
import shapeless.Tuples._

case class OrderDesc(name:String, fillingDate:DateTime, dueDate:Option[DateTime], status:OrderStatus, partdefs:List[PartDefInOrder])

case class PartDefInOrder(partDefId:Int, filter:String, count:Int)

trait Orders extends Tables with ObjectModel[OrderDesc] {
    self:DBAccess =>

    import profile.simple._
      
    def get(id:Int)(implicit session:Session) = {
        val q = for {
            ord <- Order
        } yield (ord.name, ord.fillingDate, ord.dueDate, ord.status)
        val q2 = for {
            pdef <- PartDefinitionInOrder if pdef.orderId === id
        } yield (pdef.partDefId, pdef.filter, pdef.count)
        for {
            ord <- q.firstOption
        } yield OrderDesc.tupled((ord.hlisted ::: (q2.list.map(PartDefInOrder.tupled) :: HNil)).tupled)
    }

    def insert(ord:OrderDesc)(implicit session:Session) = {
        val id:Int = Order.forInsert.insert((ord.name, ord.fillingDate, ord.dueDate, ord.status))
        PartDefinitionInOrder.insertAll(ord.partdefs.map(x=>(id, x.partDefId, x.count, x.filter)):_*) 
        id
    }

}
