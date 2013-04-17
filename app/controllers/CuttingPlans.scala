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

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] 
		with ObjectListController[CuttingPlanForList] {
    type ModelType = DBAccessConf with CuttingPlanModel with CuttingPlanList
    
	lazy val model = new DBAccessConf with DBCutPlans with DBPartDefs with CuttingPlanModel with CuttingPlanList
	
	def partDefMapping(implicit s:scala.slick.session.Session) = mapping(
			"partdefid" -> number.verifying(model.existsPartDef _),
			"count" -> number.verifying(_>0))(PartDefInCutPlan)(PartDefInCutPlan.unapply)
	
	def form(implicit s:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
			"file" -> TemporaryFileManager.tempFileMapping,
			"partdefs" -> play.api.data.Forms.list(partDefMapping)
			)(CuttingPlanDesc)(CuttingPlanDesc.unapply))
			
	def template = cutplan_form.apply
	
	def saveRoute = routes.CuttingPlans.save
	
	def updateRoute = routes.CuttingPlans.update
	
	def listTemplate = views.html.cutplan.list.apply 
}
