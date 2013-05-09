package models.order

import models.ObjectListModel
import scalaz._
import models.DBAccess

trait OrderList extends ObjectListModel[OrderForList] {
	self:DBAccess with Orders =>
		
	import profile.simple._
		
	def list(u:Unit)(implicit session:Session) = {
		Query(Order).map(x=>(x.id, x.name, x.fillingDate, x.dueDate, x.status)).
		    list.map(OrderForList.tupled)
	}
	
	type FilterType = Unit
	
	def parseFilter(str:String) = Success({})
}