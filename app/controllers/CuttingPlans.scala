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

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] {
    type ModelType = DBAccessConf with CuttingPlanModel
    
	lazy val model = new DBAccessConf with DBCutPlans with DBPartDefs with CuttingPlanModel
	
	def partDefMapping(implicit s:scala.slick.session.Session) = mapping(
			"partdefid" -> number.verifying(model.existsPartDef _),
			"count" -> number.verifying(_>0))(PartDefInCutPlan)(PartDefInCutPlan.unapply)
	
	def form(implicit s:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
			"file" -> TemporaryFileManager.tempFileMapping,
			"partdefs" -> list(partDefMapping)
			)(CuttingPlanDesc)(CuttingPlanDesc.unapply))
			
	def template = cutplan_form.apply
	
	def saveRoute = routes.CuttingPlans.save
	def updateRoute = routes.CuttingPlans.update
}
