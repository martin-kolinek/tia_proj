package models.partdef

import models._
import models.basic._

case class PartDefinitionDesc(name:String, filter:String, file:Array[Byte])

case class PartDefinitionForList(id:Int, name:String, filter:String) {
	def description = s"$name ($filter)"
}

trait PartDefinitions extends Tables {
	this:DBAccess =>
	import profile.simple._
	
    private def idQuery(id:Int) = for(pd<-PartDefinition if pd.id === id) yield (pd.name, pd.filter, pd.file)

	def getPartDef(id:Int)(implicit session:Session) = {
		val q = idQuery(id)
		q.firstOption.map(PartDefinitionDesc.tupled)
	}

    def existsPartDef(id:Int)(implicit session:Session) = {
        idQuery(id).firstOption.isDefined
    }
	
	def listPartDefinitions(implicit session:Session) = {
		val q = for(pd<-PartDefinition) yield (pd.id, (pd.name, pd.filter, pd.file))
		q.list.map(x=>WithID(Some(x._1), PartDefinitionDesc.tupled(x._2)))
	}
	
	def updatePartDef(id:Int, pd:PartDefinitionDesc)(implicit session:Session) {
		val q = for {
			dpd <- PartDefinition if dpd.id === id
		} yield dpd.file ~ dpd.filter ~ dpd.name
		q.update(pd.file, pd.filter, pd.name)
	}
	
	def hidePartDefinition(id:Int)(implicit session:Session) {
		Query(PartDefinition).filter(_.id === id).map(_.hidden).update(true)
	}
	
	def insertPartDef(pd:PartDefinitionDesc)(implicit session:Session) = {
		PartDefinition.forInsert.insert((pd.file, pd.filter, pd.name, false))
	}

	def getPartDefDescription(id:Int)(implicit session:Session) = {
		Query(PartDefinition).filter(_.id === id).map(x=>(x.id, x.name, x.filter)).firstOption.
		    map(PartDefinitionForList.tupled).map(_.description)
	}
}
