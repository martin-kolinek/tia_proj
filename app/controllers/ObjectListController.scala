package controllers

import play.api._
import play.api.mvc._
import play.api.templates.Html
import models.ObjectListModel
import models.DBAccess
import views.html.grid

trait ObjectListController[ObjectType] {
	self:Controller =>
		
	type ModelType <: DBAccess with ObjectListModel[ObjectType]
	
	def model:ModelType
	
	def header:Html
	
	def row: ObjectType => Html
	
	def list = Action { implicit request =>
		val m = model
		m.withTransaction { implicit s =>
			Ok(grid(header, m.list.map(row)))
		}
	}
}