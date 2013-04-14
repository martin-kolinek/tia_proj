package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.templates.Html
import models.ObjectModel
import models.DBAccess

trait ObjectController[ObjectType] {
	self:Controller =>
	def form:Form[ObjectType]
	
	def template: (Form[ObjectType], Call) => Html
	
	def model:DBAccess with ObjectModel[ObjectType]
	
	def saveRoute:Call
	
	def updateRoute:Int => Call
	
	def add = Action{
		Ok(template(form, saveRoute))
	}
	
	def save = Action{ implicit request =>
		val binding = form.bindFromRequest
		binding.fold(
			errFrm => BadRequest(template(form, saveRoute)),
			obj => {
				val m = model
				m.withTransaction { implicit session =>
					m.insert(obj)
					Ok("inserted")
				}
			})
	}
	
	def edit(id:Int) = Action {
		val m = model
		m.withTransaction { implicit session =>
			m.get(id) match {
				case None => BadRequest("Unknown id")
				case Some(obj) => Ok(template(form.fill(obj), updateRoute(id)))
			}
		}
	}
	
	def update(id:Int) = Action { implicit request =>
		val binding = form.bindFromRequest
		binding.fold(
				errFrm => BadRequest(template(errFrm, updateRoute(id))),
				obj => {
					val m = model
					m.withTransaction { implicit session =>
						m.update(id, obj)
						Ok("updated")
					}
				})
	}
}