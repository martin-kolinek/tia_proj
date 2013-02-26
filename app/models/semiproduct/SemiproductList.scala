package models.semiproduct

import org.joda.time.DateTime
import models.DBAccess
import models.ObjectListModel

case class PackForList(id:Int, heatNo:String, deliveryDate:DateTime, unlimited:Boolean, material:MaterialDesc, shape:ShapeDesc)

trait SemiproductList extends ObjectListModel[PackForList] {
	self:DBAccess with Semiproducts =>
	import profile.simple._
	
    def extractPackForList(shp:OptionShape, pck:DBPack, mat:DBMaterial) = 
        PackForList(pck.id, pck.heatNo, pck.deliveryDate, pck.unlimited, MaterialDesc(mat.name), extractShape(shp))

	def list(implicit s:Session) = packQuery.list.map((extractPackForList _).tupled)
}
