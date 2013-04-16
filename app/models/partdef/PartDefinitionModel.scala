package models.partdef

import models.DBAccess
import models.ObjectModel

trait PartDefinitionModel extends ObjectModel[PartDefinitionDesc] {
	self:PartDefinitions with DBAccess =>
		
	import profile.simple._
	
	def get(implicit s:Session) = getPartDef _
	
	def update(implicit s:Session) = updatePartDef _
	
	def insert(implicit s:Session) = insertPartDef _
}