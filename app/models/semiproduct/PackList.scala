package models.semiproduct

import org.joda.time.DateTime
import models.DBAccess
import models.ObjectListModel
import models.enums._

trait PackList extends ObjectListModel[PackForList] {
	self:DBAccess with Semiproducts =>
	import profile.simple._

	def list(implicit s:Session) = packQuery.list.map((extractPackForList _).tupled)
	
	def listSemiproducts(packId:Int)(implicit session:Session) = {
        val q = for {
            (sp, c) <- Semiproduct.leftJoin(Cutting).on(_.id === _.semiproductId)
            if sp.packId === packId
        } yield (sp.id, sp.serialNo, c.id.?.isNull)
        q.list.map {
            case (id, serial, used) => SemiproductForList(id, serial, if(used) SemiproductStatus.Used else SemiproductStatus.Available)
        }
	}
}
