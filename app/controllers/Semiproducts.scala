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
import org.joda.time.DateTime

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
	
	val sheetMapping = mapping(
			"thickness" -> optional(bigDecimal),
			"width" -> optional(bigDecimal),
			"height" -> optional(bigDecimal))(SheetDesc)(SheetDesc.unapply _)
	
	val circMapping = mapping(
			"thickness" -> optional(bigDecimal),
			"radius" -> optional(bigDecimal),
			"length" -> optional(bigDecimal))(CirclePipeDesc)(CirclePipeDesc.unapply _)
	
	val squareMapping = mapping(
			"thickness" -> optional(bigDecimal),
			"diameter" -> optional(bigDecimal),
			"length" -> optional(bigDecimal))(SquarePipeDesc)(SquarePipeDesc.unapply _)
			
	val materialMapping = mapping(
			"material" -> nonEmptyText)(MaterialDesc)(MaterialDesc.unapply _)

	def formPackExtract(heat:String, date:DateTime, unlim:Boolean, mat:MaterialDesc, shType:Int, sheet:SheetDesc, circ:CirclePipeDesc, square:SquarePipeDesc) = { 
		val shp = IndexedSeq(ShapeDesc, sheet, circ, square)(shType)
		PackDesc(heat, date, unlim, mat, shp)
	}
	
	def packFormExtract(pck:PackDesc) = pck match {
		case PackDesc(heat, date, unlim, mat, shp) => {
			val emptySheet = SheetDesc(None, None, None)
			val emptyCirc = CirclePipeDesc(None, None, None)
			val emptySquare = SquarePipeDesc(None, None, None)
			val (shpType, sheet, circ, square) = shp match {
				case s:SheetDesc => (1, s, emptyCirc, emptySquare)
				case c:CirclePipeDesc => (2, emptySheet, c, emptySquare)
				case s:SquarePipeDesc => (3, emptySheet, emptyCirc, s)
				case _ => (0, emptySheet, emptyCirc, emptySquare)
			}
			Some(heat, date, unlim, mat, shpType, sheet, circ, square)
		}
		case _ => None
	} 
	
	val form = Form(mapping(
			"heat" -> nonEmptyText,
			"delivery" -> jodaDate,
			"unlimited" -> boolean,
			"material" -> materialMapping,
			"type" -> number,
			"sheet" -> sheetMapping,
			"circ" -> circMapping,
			"square" -> squareMapping)(formPackExtract)(packFormExtract))
	
	def insert = Action { 
		implicit request =>
		val binding = form.bindFromRequest
		binding.fold (
				errFrm => BadRequest(views.html.semiprod_form(errFrm, routes.Semiproducts.insert)),
				pck => {
					val sp = new DBAccessConf with Semiproducts
					sp.withSession{
						implicit session =>
						sp.insertPack(pck)
					}
				}
				)
		Ok("asdf")
	}
}