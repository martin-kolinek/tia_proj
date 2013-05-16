package models.order

import models.ObjectListModel
import scalaz._
import scalaz.std.string._
import models.DBAccess

trait OrderList extends ObjectListModel[OrderForList] {
	self:DBAccess with Orders =>
		
	import profile.simple._
		
	def list(cpId:FilterType)(implicit session:Session) = {
        val q = cpId match {
            case None => Query(Order)
            case Some(id) => Query(Order).filter(ord => (for{
                odef <- OrderDefinition if odef.orderId === ord.id
                cp <- PartDefinitionInCuttingPlan if cp.partDefId === odef.partDefId && cp.cutPlanId === id
            } yield odef.id).exists)
        }
		q.map(x=>(x.id, x.name, x.fillingDate, x.dueDate, x.status)).
		    list.map(OrderForList.tupled)
	}
	
	type FilterType = Option[Int]
	
	def parseFilter(str:String) = Success(parseInt(str).toOption)
}
