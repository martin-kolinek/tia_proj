package models

trait ObjectModel[ObjectType] {
	self:DBAccess =>
	import profile.simple._
	def insert(obj:ObjectType)(implicit session:Session):Int
	def update(id:Int, obj:ObjectType)(implicit session:Session):Unit
	def get(id:Int)(implicit session:Session):Option[ObjectType]
}