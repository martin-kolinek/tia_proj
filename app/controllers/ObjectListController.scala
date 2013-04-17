package controllers

import play.api._
import play.api.mvc._
import play.api.templates.Html
import models.ObjectListModel
import models.DBAccess

trait ObjectListController[ObjectType] {
	self:Controller =>
		
	type ModelType <: DBAccess with ObjectListModel[ObjectType]
	
	def model:ModelType
	
	def listTemplate: Seq[ObjectType] => Html
	
	def list = Action { implicit request =>
		val m = model
		m.withTransaction { implicit s =>
			Ok(listTemplate(m.list))
		}
	}
}