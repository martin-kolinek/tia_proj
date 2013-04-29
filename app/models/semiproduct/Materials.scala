package models.semiproduct

import models.basic.Tables
import models.DBAccess

case class MaterialDesc(name:String) {
}

trait Materials extends Tables {
	self:DBAccess =>
		
	import profile.simple._
		
	private def getMaterialId(mat:MaterialDesc)(implicit session:Session) = 
		(for(matDb <- Material if matDb.name===mat.name) yield matDb.id).firstOption
		
	private def insertMaterial(mat:MaterialDesc)(implicit session:Session) =
		Material.forInsert.insert(mat.name)
		
	def getOrCreateMaterial(mat:MaterialDesc)(implicit session:Session) =
		getMaterialId(mat).getOrElse(insertMaterial(mat))
} 
