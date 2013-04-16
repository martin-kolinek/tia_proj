package models.order

import models.ObjectModel
import models.DBAccess

trait OrderModel extends ObjectModel[OrderDesc] {
	self:DBAccess with Orders =>
		
	import profile.simple._
	
	def get(implicit s:Session) = getOrder _
	
	def update(implicit s:Session) = updateOrder _
	
	def insert(implicit s:Session) = insertOrder _
}