package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views.html.partdef.partdef_form
import models.DBAccessConf
import models.partdef.{PartDefinitions=>DBPartDef}
import play.api.Play.current
import models.partdef.PartDefinitionDesc
import models.partdef.PartDefinitionDesc
import models.partdef.PartDefinitionModel
import models.partdef.PartDefinitionModel
import models.partdef.PartDefinitionList
import models.partdef.PartDefinitionForList
import models.semiproduct.Shapes
import play.api.templates.Html
import models.semiproduct.SemiproductFilter
import models.semiproduct.{Semiproducts => MSemiproducts}
import models.semiproduct.ShapeFilter
import play.api.libs.json.Json
import scalaz.syntax.std.string._
import scalaz.Success
import scalaz.Failure

object PartDefinitions extends Controller with ObjectController[PartDefinitionDesc]
		with ObjectListController[PartDefinitionForList]{
    type ModelType = DBAccessConf with PartDefinitionModel with PartDefinitionList 

	lazy val model = new DBAccessConf with DBPartDef with Shapes with PartDefinitionModel with PartDefinitionList with MSemiproducts with ShapeFilter with SemiproductFilter
	
	def form(implicit session:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text.verifying(SemiproductFilterHelpers.filterConstraint(model)),
			"file" -> TemporaryFileManager.tempFileMapping)
			(PartDefinitionDesc)(PartDefinitionDesc.unapply _))
	
	def saveRoute = routes.PartDefinitions.save
	def updateRoute = routes.PartDefinitions.update
    def listRoute = routes.PartDefinitions.list()
	
	def template = partdef_form.apply
	
	def listTemplates = {
        case "table" => views.html.partdef.select_list.apply
        case _ => views.html.partdef.list.apply
    }
    
    def partDefDescription(id:Int) = Action{
    	model.withTransaction {implicit s=>
    		val desc = model.getPartDefForList(id)
    		desc match {
    			case Some(pdef) => Ok(Json.obj("desc" -> pdef.description, "filter" -> pdef.filter)) 
    			case None => NotFound("part definition not found")
    		}
    	}
    }
    
    def finishedPartsTemplate : String => Seq[models.partdef.CutPart] => Html = {
    	case "table" => views.html.partdef.list_parts_table.apply
    	case _ => views.html.partdef.list_parts.apply
    }
    
    def listFinishedParts(filter:String, template:String) = Action {
    	model.withTransaction { implicit s =>
    		filter.parseInt match {
    			case Success(flt) => Ok(finishedPartsTemplate(template)(model.listFinishedParts(flt)))
    			case Failure(msg) => BadRequest(msg.getMessage()) 
    		}
    	}
    }
    
    def hidePartDefinition(id:Int) = Action {
    	model.withTransaction { implicit s=>
    		model.hidePartDefinition(id)
    		Ok("Success")
    	}
    }
}
