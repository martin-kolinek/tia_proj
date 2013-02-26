package models.cutplan

import models.basic.Tables
import models.ObjectListModel
import models.DBAccess

case class CuttingPlanForList(id:Int, name:String, filter:String)

trait CuttingPlanList extends Tables with ObjectListModel[CuttingPlanForList] {
	self:DBAccess =>
	
	import profile.simple._
	
	def list(implicit s:Session) = {
		Query(CuttingPlan).map(x=>(x.id, x.name, x.filter)).list.map(CuttingPlanForList.tupled)
	}
}