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

object PartDefinitions extends Controller with ObjectController[PartDefinitionDesc]
		with ObjectListController[PartDefinitionForList]{
    type ModelType = DBAccessConf with PartDefinitionModel with PartDefinitionList 

	lazy val model = new DBAccessConf with DBPartDef with Shapes with PartDefinitionModel with PartDefinitionList
	
	def form(implicit session:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
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
	
	def selectList = Action {
    	model.withTransaction { implicit s =>
    		Ok(views.html.partdef.select_list(model.list))
    	}
    }
    
    def partDefDescription(id:Int) = Action{
    	model.withTransaction {implicit s=>
    		Ok(model.getPartDefDescription(id).getOrElse("unknown"))
    	}
    }
    
    def finishedPartsTemplate : String => Seq[models.partdef.CutPart] => Html = {
    	case "table" => views.html.partdef.list_parts_table.apply
    	case _ => views.html.partdef.list_parts.apply
    }
    
    def listFinishedParts(template:String) = Action {
    	model.withTransaction { implicit s =>
    		Ok(finishedPartsTemplate(template)(model.listFinishedParts))
    	}
    }
}
