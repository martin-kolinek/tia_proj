package models.cutting

import models.DBAccess
import models.semiproduct.Semiproducts

case class CuttingForList(finished:Boolean, semiproduct:String, cuttingPlan:Int)

trait CuttingList {
	self:DBAccess with Semiproducts =>
	import profile.simple._
	
	/*def list(implicit session:Session) = for {
		
	} yield*/
	
}