package models.cutplan

import models.ObjectModel
import models.DBAccess

trait CuttingPlanModel extends ObjectModel[CuttingPlanDesc] {
	self:DBAccess with CuttingPlans =>
		
	import profile.simple._
		
	def get(implicit s:Session) = getCuttingPlan _
	
	def insert(implicit s:Session) = insertCuttingPlan _
	
	def update(implicit s:Session) = updateCuttingPlan _
	
}