package controllers

import play.api._
import scalaz._
import scalaz.Validation
import play.api.mvc._
import play.api.templates.Html
import models.ObjectListModel
import models.DBAccess

trait ObjectListController[ObjectType] {
	self:Controller =>
		
	type ModelType <: DBAccess with ObjectListModel[ObjectType]
	
	def model:ModelType
	
	def listTemplates: String => Seq[ObjectType] => Html
	
	def list(filter:String, template:String = "") = Action { implicit request =>
		val m = model
		val filt = m.parseFilter(filter)
		filt match {
			case Success(filt) => m.withTransaction { implicit s =>
			    Ok(listTemplates(template)(m.list(filt)))
			}
			case Failure(err) => BadRequest(err)
		}
		
	}
}
