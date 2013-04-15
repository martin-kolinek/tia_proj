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

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] {
    type ModelType = DBAccessConf with DBCutPlans
    
	lazy val model = new DBAccessConf with DBCutPlans
	lazy val parts = new DBAccessConf with DBPartDefs
	
	def partDefMapping(implicit s:scala.slick.session.Session) = mapping(
			"partdefid" -> number.verifying(parts.exists _),
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
