package models.cutplan

import models.basic.Tables
import scalaz._
import models.ObjectListModel
import models.DBAccess

case class CuttingPlanForList(id:Int, name:String, filter:String, canEdit:Boolean)

trait CuttingPlanList extends Tables with ObjectListModel[CuttingPlanForList] {
	self:DBAccess =>
	
	import profile.simple._
	
	type FilterType = Unit
	
	def list(u:Unit)(implicit s:Session) = {
		Query(CuttingPlan).filter(_.hidden === false).
            map(x=>(x.id, x.name, x.filter, !Query(Cutting).filter(_.cuttingPlanId === x.id).exists)).
            list.map(CuttingPlanForList.tupled)
	}
	def parseFilter(str:String) = Success({})
}
