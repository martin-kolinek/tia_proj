package controllers

import play.api._
import models.semiproduct.{Semiproducts => MSemiproducts}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.order.OrderDesc
import models.order.Orders
import models.DBAccessConf
import play.api.Play.current
import models.enums._
import models.partdef.{PartDefinitions => DBPartDef}
import views.html.order.order_form
import models.order.OrderModel
import models.order.OrderForList
import models.order.OrderList
import models.order.OrderDefinitionDesc
import models.order.PartInOrder
import models.order.OrderDefStatus
import models.order.OrderDefForList
import play.api.templates.Html
import models.semiproduct.Shapes
import models.semiproduct.ShapeFilter
import models.semiproduct.SemiproductFilter
import play.api.libs.json.Json
import scalaz._
import scalaz.std.string._

object OrderController extends Controller with ObjectController[OrderDesc] 
		with ObjectListController[OrderForList] {
	type ModelType = DBAccessConf with OrderModel with OrderList
	lazy val model = new DBAccessConf with Orders with DBPartDef with Shapes with OrderModel with OrderList with MSemiproducts with ShapeFilter with SemiproductFilter
	
	def pdefMapping(implicit s:scala.slick.session.Session) = mapping(
			"id" -> optional(number),
			"pdefid" -> number.verifying(model.existsPartDef _),
			"filter" -> text.verifying(SemiproductFilterHelpers.filterConstraint(model)),
			"count" -> number(1))(OrderDefinitionDesc)(OrderDefinitionDesc.unapply)
	
	def form(implicit s:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filling_date" -> jodaDate,
			"due_date" -> optional(jodaDate),
			"odefs" -> play.api.data.Forms.list(pdefMapping))
			(OrderDesc)(OrderDesc.unapply))
			
	def template = order_form.apply
	
	def saveRoute = routes.OrderController.save
	
	def updateRoute = routes.OrderController.update _

    def listRoute = routes.OrderController.list()
	
	def listTemplates = {
		case "table" => views.html.order.list_select.apply
        case _ => views.html.order.list.apply
    }

    def statusForm = Form(single(
        "statuses" -> play.api.data.Forms.list(mapping(
            "odefid" -> number,
            "parts" -> play.api.data.Forms.list(mapping(
                "shape" -> number,
                "material" -> number,
                "count" -> number
            )(PartInOrder)(PartInOrder.unapply))
        )(OrderDefStatus)(OrderDefStatus.unapply))))

    def status(id:Int) = Action{
        model.withTransaction { implicit s =>
            val frm = statusForm.fill(model.orderStatus(id))
            Ok(views.html.order.status(frm, routes.OrderController.updateStatus(id)))
        }
    }

    def updateStatus(id:Int) = Action{ implicit r =>
        val binding = statusForm.bindFromRequest
        binding.fold(
            errFrm => BadRequest(views.html.order.status(errFrm, routes.OrderController.updateStatus(id))),
            statuses => {
                model.withTransaction { implicit s =>
                    model.updateOrderStatus(id, statuses)
                    Redirect(listRoute)
                }
            }
        )
    }
    
    def listDefTemplates:String => Seq[OrderDefForList] => Html = {
    	case "dropdown" => views.html.order.definitions_dropdown.apply
    	case _ => views.html.order.definitions.apply
    }

    def listDefinitions(id:Int, filter:String, template:String) = Action{
        model.withTransaction { implicit s =>
            Ok(listDefTemplates(template)(model.listOrderDefs(id, parseInt(filter).toOption)))
        }
    }
    
    def orderDefDescription(id:Int) = Action {
    	model.withTransaction { implicit s =>
    		model.orderDefDescription(id) match {
    			case Some(odef) => Ok(Json.obj("desc" -> odef.description, "filter" -> odef.filter))
    			case None => NotFound("Order definition not found")
    		} 
    		
    	}
    }
}
