package models.cutting

import org.joda.time.DateTime
import models.DBAccess
import models.basic.Tables
import models.ObjectModel
import shapeless._
import shapeless.HList._
import shapeless.Tuples._

case class CuttingDesc(semiprodId:Int, cutPlanId:Int, parts:List[PartInCuttingDesc])

case class PartInCuttingDesc(partDefId:Int, OrderId:Int, count:Int)

trait Cuttings extends Tables with ObjectModel[CuttingDesc] {
    self:DBAccess =>

    import profile.simple._

    def get(id:Int)(implicit s:Session) = {
        val cutting = Query(Cutting).filter(_.id === id).filter(_.finishTime.isNull).map(x=>(x.semiproductId, x.cuttingPlanId)).
            firstOption
        val parts = Query(Part).filter(_.cuttingId === id).filter(_.orderId.isNotNull).groupBy(x=>x.partDefId -> x.orderId).map{
            case ((partDefId, orderId), rows) => (partDefId, orderId.get, rows.length)
        }.list.map(PartInCuttingDesc.tupled)
        cutting.map(x=>(x.hlisted ::: parts :: HNil).tupled).map(CuttingDesc.tupled)
    }

    def insert(cut:CuttingDesc)(implicit s:Session) = {
        Cutting.forInsert.insert(None, cut.semiprodId, cut.cutPlanId)
    }

    def update(id:Int, cut:CuttingDesc)(implicit s:Session) {
        Cutting.filter(_.id === id).map(x=>x.semiproductId ~ x.cuttingPlanId).update(cut.semiprodId, cut.cutPlanId)
    }
}
