package models.partdef

import models.ObjectListModel
import scalaz._
import models.DBAccess

trait PartDefinitionList extends ObjectListModel[PartDefinitionForList] {
	self:DBAccess with PartDefinitions =>
		
	import profile.simple._
		
	def list(u:Unit)(implicit s:Session) = {
	    Query(PartDefinition).filter(_.hidden === false).map(x=>(x.id, x.name, x.filter)).
	        list.map(PartDefinitionForList.tupled)
	}
	
	type FilterType = Unit
	
	def parseFilter(str:String) = Success({})
}