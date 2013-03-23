package controllers

import play.api._
import models.semiproduct._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import views.Grid
import views.Semiproducts._
import models.DBAccessConf

object Semiproducts extends Controller {
	def list = Action {
		val sp = new DBAccessConf with Semiproducts
		sp.withSession {implicit session =>
			Ok(views.html.semiproducts(Grid.grid(sp.listPacks)))
		}
	}
	
}