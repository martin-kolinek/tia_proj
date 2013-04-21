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
	def form(implicit session:scala.slick.session.Session):Form[ObjectType]
	
	def template: (Form[ObjectType], Call, String) => Html
	
    type ModelType <: DBAccess with ObjectModel[ObjectType]

	def model:ModelType
	
	def saveRoute:Call
	
	def updateRoute:Int => Call

    def listRoute:Call
	
	def add = Action{
        val m = model
        m.withTransaction {
            implicit s=>
		    Ok(template(form, saveRoute, "Add"))
        }
	}
	
	def save = Action{ implicit request =>
        val m = model
        m.withTransaction { 
            implicit session =>
		    val binding = form.bindFromRequest
		    binding.fold(
			    errFrm => { 
			    	println(errFrm.errors)
			    	BadRequest(template(errFrm, saveRoute, "Add"))
			    },
			    obj => {
					m.insert.apply(obj)
					Redirect(listRoute)
			    })
        }
	}
	
	def edit(id:Int) = Action {
		val m = model
		m.withTransaction { implicit session =>
			m.get.apply(id) match {
				case None => NotFound
				case Some(obj) => Ok(template(form.fill(obj), updateRoute(id), "Edit"))
			}
		}
	}
	
	def update(id:Int) = Action { implicit request =>
        val m = model
		m.withTransaction { 
            implicit session =>
		    val binding = form.bindFromRequest
		    binding.fold(
				errFrm => BadRequest(template(errFrm, updateRoute(id), "Edit")),
				obj => {
				    m.update.apply(id, obj)
				    Redirect(listRoute)
				})
	    }
    }
}
