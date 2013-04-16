package models.cutting

import models.ObjectModel
import models.DBAccess

trait CuttingModel extends ObjectModel[CuttingDesc] {
	self : Cuttings with DBAccess =>
		
	import profile.simple._
		
	def get(implicit s:Session) = getCutting _
	
	def insert(implicit s:Session) = insertCutting _
	
	def update(implicit s:Session) = updateCutting _
		
}