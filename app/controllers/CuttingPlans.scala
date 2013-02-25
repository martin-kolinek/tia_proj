package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.cutplan.CuttingPlanDesc
import models.DBAccessConf
import play.api.Play.current
import models.cutplan.{CuttingPlans => DBCutPlans}
import models.cutplan.PartDefInCutPlan
import models.cutplan.CuttingPlanDesc
import views.html.cutplan.cutplan_form

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] {
    type ModelType = DBAccessConf with DBCutPlans
	def model = new DBAccessConf with DBCutPlans
	
	def partDefMapping(m:ModelType)(implicit s:m.profile.simple.Session) = mapping(
			"partdefid" -> number,
			"count" -> number.verifying(_>0))(PartDefInCutPlan)(PartDefInCutPlan.unapply)
	
	def form(m:ModelType)(implicit s:m.profile.simple.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
			"file" -> TemporaryFileManager.tempFileMapping,
			"partdefs" -> list(partDefMapping(m))
			)(CuttingPlanDesc)(CuttingPlanDesc.unapply))
			
	def template = cutplan_form.apply
	
	def saveRoute = routes.CuttingPlans.save
	def updateRoute = routes.CuttingPlans.update
}
