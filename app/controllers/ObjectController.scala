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
	def form(m:ModelType)(implicit session:m.profile.simple.Session):Form[ObjectType]
	
	def template: (Form[ObjectType], Call) => Html
	
    type ModelType <: DBAccess with ObjectModel[ObjectType]

	def model:ModelType
	
	def saveRoute:Call
	
	def updateRoute:Int => Call
	
	def add = Action{
        val m = model
        m.withTransaction {
            implicit s=>
		    Ok(template(form(m), saveRoute))
        }
	}
	
	def save = Action{ implicit request =>
        val m = model
        m.withTransaction { 
            implicit session =>
		    val binding = form(m).bindFromRequest
		    binding.fold(
			    errFrm => BadRequest(template(form(m), saveRoute)),
			    obj => {
					m.insert(obj)
					Ok("inserted")
			    })
        }
	}
	
	def edit(id:Int) = Action {
		val m = model
		m.withTransaction { implicit session =>
			m.get(id) match {
				case None => BadRequest("Unknown id")
				case Some(obj) => Ok(template(form(m).fill(obj), updateRoute(id)))
			}
		}
	}
	
	def update(id:Int) = Action { implicit request =>
        val m = model
		m.withTransaction { 
            implicit session =>
		    val binding = form(m).bindFromRequest
		    binding.fold(
				errFrm => BadRequest(template(errFrm, updateRoute(id))),
				obj => {
				    m.update(id, obj)
				    Ok("updated")
				})
	    }
    }
}
