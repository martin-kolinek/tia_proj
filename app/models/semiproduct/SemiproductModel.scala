package models.semiproduct

import models.DBAccess
import models.ObjectModel

trait SemiproductModel extends ObjectModel[PackDesc] {
	self:DBAccess with Semiproducts =>
		
	import profile.simple._
	
	def get(implicit s:Session) = getPack _
	def update(implicit s:Session) = updatePack _
	def insert(implicit s:Session) = insertPack _
}