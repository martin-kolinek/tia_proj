package models.partdef

import models.ObjectListModel
import models.DBAccess

trait PartDefinitionList extends ObjectListModel[PartDefinitionForList] {
	self:DBAccess with PartDefinitions =>
		
	import profile.simple._
		
	def list(implicit s:Session) = {
	    Query(PartDefinition).map(x=>(x.id, x.name, x.filter)).
	        list.map(PartDefinitionForList.tupled)
	}
}