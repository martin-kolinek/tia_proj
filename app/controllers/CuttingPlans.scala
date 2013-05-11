package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.cutplan.CuttingPlanDesc
import models.DBAccessConf
import play.api.Play.current
import models.cutplan.{CuttingPlans => DBCutPlans}
import models.partdef.{PartDefinitions => DBPartDefs}
import models.cutplan.PartDefInCutPlan
import models.cutplan.CuttingPlanDesc
import views.html.cutplan.cutplan_form
import models.cutplan.CuttingPlanModel
import models.cutplan.CuttingPlanForList
import models.cutplan.CuttingPlanList
import models.semiproduct.Shapes
import models.semiproduct.SemiproductFilter
import models.semiproduct.ShapeFilter
import models.semiproduct.{Semiproducts => MSemiproducts}
import play.api.libs.json.Json

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] 
		with ObjectListController[CuttingPlanForList] {
    type ModelType = DBAccessConf with CuttingPlanModel with CuttingPlanList
    
	lazy val model = new DBAccessConf with DBCutPlans with DBPartDefs with Shapes with CuttingPlanModel with CuttingPlanList with ShapeFilter with SemiproductFilter with MSemiproducts
	
	def partDefMapping(implicit s:scala.slick.session.Session) = mapping(
			"partdefid" -> number.verifying(model.existsPartDef _),
			"count" -> number.verifying(_>0))(PartDefInCutPlan)(PartDefInCutPlan.unapply)
	
	def form(implicit s:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text.verifying(SemiproductFilterHelpers.filterConstraint(model)),
			"file" -> TemporaryFileManager.tempFileMapping,
			"partdefs" -> play.api.data.Forms.list(partDefMapping)
			)(CuttingPlanDesc)(CuttingPlanDesc.unapply))
			
	def template = cutplan_form.apply
	
	def saveRoute = routes.CuttingPlans.save
	
	def updateRoute = routes.CuttingPlans.update

    def listRoute = routes.CuttingPlans.list()
	
	def listTemplates = {
        case _ => views.html.cutplan.list.apply 
    }
    
    def cutPlanDescription(id:Int) = Action {
    	model.withTransaction { implicit s =>
    		model.cutPlanDescription(id) match {
    			case None => NotFound("Cutting plan not found")
    			case Some(cp) => Ok(Json.obj("desc" -> cp.name, "filter" -> cp.fullFilter))
    		}
    		
    		
    	}
    }
}
