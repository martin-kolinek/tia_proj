package models.cutting

import models.DBAccess
import scalaz._
import models.semiproduct.Semiproducts
import models.cutplan.CuttingPlanForList
import models.cutplan.CuttingPlans
import models.semiproduct.SemiproductForList
import models.ObjectListModel

trait CuttingList extends ObjectListModel[CuttingForList] {
	self:DBAccess with Semiproducts with CuttingPlans =>
	import profile.simple._
	
	def list(u:Unit)(implicit session:Session) = {
		val q = for {
			cut <- Cutting 
			sp <- Semiproduct if cut.semiproductId === sp.id
			pckq@(shp, pck, mat) <- packQuery  if pck.id === sp.packId
			cp <- CuttingPlan if cut.cuttingPlanId === cp.id
		} yield (pckq, sp.serialNo, cp.name, cut.id, cut.finishTime)
		q.list.map {
			case (pck, serial, cp, cutid, fin) => CuttingForList(cutid,
					cp,
					serial,
					(extractPackForList _).tupled(pck),
					fin)
		}
	}
	
	type FilterType = Unit
	
	def parseFilter(str:String) = Success({})
}
