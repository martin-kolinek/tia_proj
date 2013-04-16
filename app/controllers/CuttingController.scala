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
import models.cutting.CuttingModel

object CuttingController extends Controller with ObjectController[CuttingDesc] {
    type ModelType = DBAccessConf with CuttingModel 
    lazy val model = new DBAccessConf with Cuttings 
        with DBSP with DBCP with DBPartDefs 
        with DBOrders with CuttingModel

    def partMapping(implicit s:scala.slick.session.Session) = mapping(
        "partdef" -> number.verifying(model.existsPartDef _),
        "order" -> number.verifying(model.existsOrder _),
        "count" -> number.verifying(_>0))(PartInCuttingDesc)(PartInCuttingDesc.unapply)

    def form(implicit s:scala.slick.session.Session) = Form(mapping(
        "semiproduct" -> number.verifying(model.existsSemiproduct _),
        "cutting_plan" -> number.verifying(model.existsCuttingPlan _),
        "parts" -> list(partMapping)
    )(CuttingDesc)(CuttingDesc.unapply))

    def template = cutting_form.apply

    def saveRoute = routes.CuttingController.save

    def updateRoute = routes.CuttingController.update _
}
