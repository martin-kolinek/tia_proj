package models

trait ObjectModel[ObjectType] {
	self:DBAccess =>
	import profile.simple._
	def insert(implicit s:Session): ObjectType => Int
	def update(implicit s:Session): (Int, ObjectType) => Unit
	def get(implicit s:Session): Int => Option[ObjectType]
}
