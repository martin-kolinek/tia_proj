package models.semiproduct

import org.joda.time.DateTime
import models.DBAccess
import models.ObjectListModel

case class PackForList(id:Int, heatNo:String, deliveryDate:DateTime, unlimited:Boolean, material:MaterialDesc, shape:ShapeDesc)

trait SemiproductList extends ObjectListModel[PackForList] {
	self:DBAccess with Semiproducts =>
	import profile.simple._
	
	//def list(implicit s:Session) = packQuery.list
}