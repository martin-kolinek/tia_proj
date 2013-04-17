package models.cutting

import models.DBAccess
import models.semiproduct.Semiproducts
import models.cutplan.CuttingPlanForList
import models.cutplan.CuttingPlans
import models.semiproduct.SemiproductForList
import models.ObjectListModel

trait CuttingList extends ObjectListModel[CuttingForList] {
	self:DBAccess with Semiproducts with CuttingPlans =>
	import profile.simple._
	
	def list(implicit session:Session) = {
		val q = for {
			pckq@(shp, mat, pck) <- packQuery
			sp <- Semiproduct if pck.id === sp.packId
			cut <- Cutting if cut.semiproductId === sp.id
			cp <- CuttingPlan if cut.cuttingPlanId === cp.id
		} yield (pckq, semiproductProjection(sp), cuttingPlanProjection(cp), cut.id)
		q.list.map {
			case (pck, sp, cp, cutid) => CuttingForList(cutid,
					CuttingPlanForList.tupled(cp),
					SemiproductForList.tupled(sp),
					(extractPackForList _).tupled(pck))
		}
	}
	
}