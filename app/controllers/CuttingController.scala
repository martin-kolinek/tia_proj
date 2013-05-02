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
import models.cutting.CuttingForList
import models.cutting.CuttingList
import models.cutting.FinishedPartInCutting

object CuttingController extends Controller with ObjectController[CuttingDesc] 
		with ObjectListController[CuttingForList] {
    type ModelType = DBAccessConf with CuttingModel with CuttingList 
    lazy val model = new DBAccessConf with Cuttings with CuttingList
        with DBSP with DBPartDefs
        with DBOrders with CuttingModel
        
    def partMapping(implicit s:scala.slick.session.Session) = mapping(
        "partdef" -> number.verifying(model.existsPartDef _),
        "order_def" -> number.verifying(model.existsOrderDefinition _),
        "count" -> number.verifying(_>0))(PartInCuttingDesc)(PartInCuttingDesc.unapply)

    def form(implicit s:scala.slick.session.Session) = Form(mapping(
        "semiproduct" -> number.verifying(model.existsSemiproduct _),
        "cutting_plan" -> number.verifying(model.existsCuttingPlan _),
        "parts" -> play.api.data.Forms.list(partMapping)
    )(CuttingDesc)(CuttingDesc.unapply))

    def template = cutting_form.apply

    def saveRoute = routes.CuttingController.save

    def updateRoute = routes.CuttingController.update _

    def listRoute = routes.CuttingController.list()
    
    def listTemplates = {
        case _ => views.html.cutting.list.apply
    }

    def finMapping = mapping(
            "partdefid" -> number,
            "order" -> optional(number),
            "dmgcount" -> number
            )(FinishedPartInCutting)(FinishedPartInCutting.unapply)
            
    def finishForm = Form(single(
        "parts" -> play.api.data.Forms.list(finMapping
        )))

    def finish(id:Int) = Action {
        model.withTransaction {implicit s =>
            Ok(views.html.cutting.finish_form(finishForm.fill(model.getDamagedPartCounts(id)),
            		routes.CuttingController.updateFinish(id)))
        }
    }

    def updateFinish(id:Int) = Action { 
        implicit req =>
        val binding = finishForm.bindFromRequest
        binding.fold(
            errFrm => BadRequest(views.html.cutting.finish_form(errFrm, 
            		routes.CuttingController.updateFinish(id))),
            cnts => {
                model.withTransaction { implicit s =>
                    model.updateFinished(id, cnts)
                }
                Redirect(listRoute)
            })
    }

    def listParts(id:Int) = Action {
        model.withTransaction { implicit s =>
            Ok(views.html.cutting.parts(model.listParts(id)))
        }
    }
}
