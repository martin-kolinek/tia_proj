package models

trait ObjectListModel[ObjectType] {
	self:DBAccess =>
	import profile.simple._
	def list(implicit session:Session):List[ObjectType]
}