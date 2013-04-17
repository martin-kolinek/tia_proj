package models.semiproduct

import org.joda.time.DateTime
import models.DBAccess
import models.ObjectListModel

trait PackList extends ObjectListModel[PackForList] {
	self:DBAccess with Semiproducts =>
	import profile.simple._

	def list(implicit s:Session) = packQuery.list.map((extractPackForList _).tupled)
	
	def listSemiproducts(packId:Int)(implicit session:Session) = {
		Query(Semiproduct).filter(_.packId===packId).map(semiproductProjection).list.
		    map(SemiproductForList.tupled)
	}
}
