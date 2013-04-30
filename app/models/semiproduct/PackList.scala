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
        } yield (sp.id, sp.serialNo, c.id.?.isNotNull, c.finishTime.?.isNotNull)
        q.list.map {
            case (id, serial, reserved, used) => {
            	val status = (reserved, used) match {
            		case (false, _) => SemiproductStatus.Available
            		case (true, false) => SemiproductStatus.Reserved
            		case (true, true) => SemiproductStatus.Used
            	}
            	SemiproductForList(id, serial, status)
            } 
        }
	}
}
