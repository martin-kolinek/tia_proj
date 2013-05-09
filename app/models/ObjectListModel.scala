package models

import scalaz.Validation

trait ObjectListModel[ObjectType] {
	self:DBAccess =>
	import profile.simple._
	type FilterType
	def parseFilter(str:String):Validation[String, FilterType]
	def list(flt:FilterType)(implicit session:Session):List[ObjectType]
}
