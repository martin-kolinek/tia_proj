package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.cutting.CuttingDesc
import models.cutting.Cuttings
import models.DBAccessConf
import play.api.Play.current
import models.semiproduct.{Semiproducts => DBSP}
import models.cutplan.{CuttingPlans => DBCP}
import models.partdef.{PartDefinitions => DBPartDefs}
import models.order.{Orders => DBOrders}
import models.cutting.PartInCuttingDesc
import views.html.cutting.cutting_form

object CuttingController extends Controller with ObjectController[CuttingDesc] {
    type ModelType = DBAccessConf with Cuttings
    lazy val model = new DBAccessConf with Cuttings
    lazy val semiprods = new DBAccessConf with DBSP
    lazy val cutplans = new DBAccessConf with DBCP
    lazy val partdef = new DBAccessConf with DBPartDefs
    lazy val ord = new DBAccessConf with DBOrders

    def partMapping(implicit s:scala.slick.session.Session) = mapping(
        "partdef" -> number.verifying(partdef.exists _),
        "order" -> number.verifying(ord.exists _),
        "count" -> number.verifying(_>0))(PartInCuttingDesc)(PartInCuttingDesc.unapply)

    def form(implicit s:scala.slick.session.Session) = Form(mapping(
        "semiproduct" -> number.verifying(semiprods.existsSemiproduct _),
        "cutting_plan" -> number.verifying(cutplans.exists _),
        "parts" -> list(partMapping)
    )(CuttingDesc)(CuttingDesc.unapply))

    def template = cutting_form.apply

    def saveRoute = routes.CuttingController.save

    def updateRoute = routes.CuttingController.update _
}
