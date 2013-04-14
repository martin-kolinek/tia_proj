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

object PartDefinitions extends Controller with ObjectController[PartDefinitionDesc]{
	def model = new DBAccessConf with DBPartDef
	
	val form = Form(mapping(
			"name" -> nonEmptyText,
			"filter" -> text,
			"file" -> TemporaryFileManager.tempFileMapping)
			(PartDefinitionDesc)(PartDefinitionDesc.unapply _))
	
	def saveRoute = routes.PartDefinitions.save
	def updateRoute = routes.PartDefinitions.update
	
	def template = partdef_form.apply
}