package models.partdef

import models._
import models.basic._

case class PartDefinitionDesc(name:String, filter:String, file:Array[Byte])

trait PartDefinitions extends Tables with ObjectModel[PartDefinitionDesc] {
	this:DBAccess =>
	import profile.simple._
	
    def idQuery(id:Int) = for(pd<-PartDefinition if pd.id === id) yield (pd.name, pd.filter, pd.file)

	def get(id:Int)(implicit session:Session) = {
		val q = idQuery(id)
		q.firstOption.map(PartDefinitionDesc.tupled)
	}

    def exists(id:Int)(implicit session:Session) = {
        idQuery(id).firstOption.isDefined
    }
	
	def listPartDefinitions(implicit session:Session) = {
		val q = for(pd<-PartDefinition) yield (pd.id, (pd.name, pd.filter, pd.file))
		q.list.map(x=>WithID(Some(x._1), PartDefinitionDesc.tupled(x._2)))
	}
	
	def update(id:Int, pd:PartDefinitionDesc)(implicit session:Session) {
		val q = for {
			dpd <- PartDefinition if dpd.id === id
		} yield dpd.file ~ dpd.filter ~ dpd.name
		q.update(pd.file, pd.filter, pd.name)
	}
	
	def hidePartDefinition(id:Int)(implicit session:Session) {
		Query(PartDefinition).filter(_.id === id).map(_.hidden).update(true)
	}
	
	def insert(pd:PartDefinitionDesc)(implicit session:Session) = {
		PartDefinition.forInsert.insert((pd.file, pd.filter, pd.name, false))
	}
}
