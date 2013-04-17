package models.cutplan

import models.DBAccess
import models.basic.Tables
import models.ObjectModel

case class CuttingPlanDesc(name:String, filter:String, file:Array[Byte], partdefs:List[PartDefInCutPlan])

case class PartDefInCutPlan(partDefId:Int, count:Int)

trait CuttingPlans extends Tables {
	self:DBAccess =>
		
	import profile.simple._

	def getCuttingPlan(id:Int)(implicit session:Session) = {
		val q = for {
			cp <- CuttingPlan if cp.id === id
		} yield (cp.name, cp.filter, cp.file)
		for{
			(nm, filt, file) <- q.firstOption
			pdefs = getPartDefs(id)
		} yield CuttingPlanDesc(nm, filt, file, pdefs)
	}

    def existsCuttingPlan(id:Int)(implicit session:Session) = 
        Query(CuttingPlan).filter(_.id===id).firstOption.isDefined
	
	private def getPartDefs(cutPlanId:Int)(implicit session:Session) = {
		(for {
			pdef <- PartDefinitionInCuttingPlan if pdef.cutPlanId === cutPlanId
		} yield pdef.partDefId -> pdef.count).list.map(PartDefInCutPlan.tupled)
	}
	
	def insertCuttingPlan(cp:CuttingPlanDesc)(implicit session:Session) = {
		val id = CuttingPlan.forInsert.insert((cp.name, cp.filter, cp.file, false))
		PartDefinitionInCuttingPlan.insertAll(cp.partdefs.map(x=>(id, x.partDefId, x.count)):_*)
		id
	}
	
	def updateCuttingPlan(id:Int, cp:CuttingPlanDesc)(implicit session:Session) {
		Query(CuttingPlan).filter(_.id === id).map(cp=>cp.name ~ cp.filter ~ cp.file).
		update(cp.name, cp.filter, cp.file)
		(for{
			pdef <- PartDefinitionInCuttingPlan 
			if pdef.cutPlanId === id
			if !pdef.partDefId.inSet(cp.partdefs.map(_.partDefId))
		} yield pdef).delete
		for(pdef <- cp.partdefs) {
			val upd = for{
				dbpdef <- PartDefinitionInCuttingPlan
				if dbpdef.cutPlanId===id && dbpdef.partDefId===pdef.partDefId
			} yield dbpdef.count
			
			if(upd.update(pdef.count) == 0) {
				PartDefinitionInCuttingPlan.insert(id, pdef.partDefId, pdef.count)
			}
		}
	}
	
	def cuttingPlanProjection(cp:CuttingPlan.type) = (cp.id, cp.name, cp.filter)
}
