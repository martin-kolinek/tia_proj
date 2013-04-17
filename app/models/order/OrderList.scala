package models.order

import models.ObjectListModel
import models.DBAccess

trait OrderList extends ObjectListModel[OrderForList] {
	self:DBAccess with Orders =>
		
	import profile.simple._
		
	def list(implicit session:Session) = {
		Query(Order).map(x=>(x.id, x.name, x.fillingDate, x.dueDate, x.status)).
		    list.map(OrderForList.tupled)
	}
}