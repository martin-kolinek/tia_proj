package controllers

import play.api._
import models.semiproduct._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import models.DBAccessConf
import play.api.templates.Html
import org.omg.CosNaming.NamingContextPackage.NotFound
import org.joda.time.DateTime
import models.WithID
import models.semiproduct.{Semiproducts => DBSemiprods}
import views.html.semiproduct.semiprod_form

object Semiproducts extends Controller with ObjectController[PackDesc] with ObjectListController[PackForList] { 
    type ModelType = DBAccessConf with SemiproductModel with PackList
	
	lazy val model = new DBAccessConf with DBSemiprods with SemiproductModel with PackList
	
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

	def formPackExtract(heat:String, date:DateTime, unlim:Boolean, mat:MaterialDesc, shType:Int, sheet:SheetDesc, circ:CirclePipeDesc, square:SquarePipeDesc, semiprods:List[WithID[SemiproductDesc]]) = { 
		val shp = IndexedSeq(UnknownShapeDesc, sheet, circ, square)(shType)
		PackDesc(heat, date, unlim, mat, shp, semiprods)
	}
	
	def packFormExtract(pck:PackDesc) = pck match {
		case PackDesc(heat, date, unlim, mat, shp, semiprods) => {
			val emptySheet = SheetDesc(None, None, None)
			val emptyCirc = CirclePipeDesc(None, None, None)
			val emptySquare = SquarePipeDesc(None, None, None)
			val (shpType, sheet, circ, square) = shp match {
				case s:SheetDesc => (1, s, emptyCirc, emptySquare)
				case c:CirclePipeDesc => (2, emptySheet, c, emptySquare)
				case s:SquarePipeDesc => (3, emptySheet, emptyCirc, s)
				case _ => (0, emptySheet, emptyCirc, emptySquare)
			}
			Some(heat, date, unlim, mat, shpType, sheet, circ, square, semiprods)
		}
		case _ => None
	} 

	val spMapping = mapping(
			"id" -> optional(number),
			"serial" -> nonEmptyText)((id, serial)=>WithID(id, SemiproductDesc(serial))){
		case WithID(id, SemiproductDesc(serial)) => Some((id, serial))
		case _ => None
	}
	
	val packMapping = mapping(
			"heat" -> nonEmptyText,
			"delivery" -> jodaDate,
			"unlimited" -> boolean,
			"material" -> materialMapping,
			"type" -> number,
			"sheet" -> sheetMapping,
			"circ" -> circMapping,
			"square" -> squareMapping,
			"semiproducts" -> play.api.data.Forms.list(spMapping)
			)(formPackExtract)(packFormExtract)
			
	def form(implicit session:scala.slick.session.Session) = Form(packMapping)
	
	def template = semiprod_form.apply
	
	def saveRoute = routes.Semiproducts.save
	
	def updateRoute = routes.Semiproducts.update

    def listRoute = routes.Semiproducts.list()
	
	def listTemplates = {
        case "table" => views.html.semiproduct.list_select.apply
        case _ => views.html.semiproduct.list.apply
    }

    def spListTemplates: String => Seq[models.semiproduct.SemiproductForList] => Html = {
        case "table" => views.html.semiproduct.list_semiprod.apply
        case "dropdown" => views.html.semiproduct.list_semiprod_dropdown.apply
        case "_" => views.html.semiproduct.list_semiprod_main.apply
    }

    def listSemiproducts(id:Int, template:String) = Action {
        model.withTransaction { implicit s=>
            Ok(spListTemplates(template)(model.listSemiproducts(id)))
        }
    }
    
    def getSemiproductDescription(id:Int) = Action {
    	model.withTransaction { implicit s =>
    		Ok(model.getSemiproductDescription(id).getOrElse("unknown"))
    	}
    }
}
