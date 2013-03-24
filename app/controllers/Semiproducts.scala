package controllers

import play.api._
import models.semiproduct._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import views.KeyValUtils
import views.Semiproducts._
import models.DBAccessConf
import play.api.templates.Html
import org.omg.CosNaming.NamingContextPackage.NotFound

object Semiproducts extends Controller {
	def list = Action { implicit request:RequestHeader =>
		val sp = new DBAccessConf with Semiproducts
		sp.withSession {implicit session =>
			Ok(views.html.semiproducts(KeyValUtils.grid(sp.listPacks)))
		}
	}
	
	def details(packId:Int) = Action { implicit request:RequestHeader =>
		val sp = new DBAccessConf with Semiproducts
		sp.withSession{ implicit session=>
		    sp.packDetails(packId) match {
		    	case Some((desc, sps)) => Ok(views.html.semiprod_details(
		    			KeyValUtils.details(desc), 
		    			KeyValUtils.grid(sps)))
		    	case None => NotFound("Pack not found")
		    }
		}
	}
	
}