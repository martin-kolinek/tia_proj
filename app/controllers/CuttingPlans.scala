package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.cutplan.CuttingPlanDesc
import models.DBAccessConf
import play.api.Play.current
import models.cutplan.{CuttingPlans => DBCutPlans}
import models.cutplan.PartDefInCutPlanDesc
import models.cutplan.PartDefInCutPlanDesc
import models.cutplan.CuttingPlanDesc
import views.html.cutplan.cutplan_form

object CuttingPlans extends Controller with ObjectController[CuttingPlanDesc] {
	def model = new DBAccessConf with DBCutPlans
	
	val partDefMapping = mapping(
			"partdefid" -> number,
			"count" -> number.verifying(_>0))(PartDefInCutPlanDesc)(PartDefInCutPlanDesc.unapply)
	
	val form = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
			"file" -> TemporaryFileManager.tempFileMapping,
			"partdefs" -> list(partDefMapping)
			)(CuttingPlanDesc)(CuttingPlanDesc.unapply))
			
	def template = cutplan_form.apply
	
	def saveRoute = routes.CuttingPlans.save
	def updateRoute = routes.CuttingPlans.update
}