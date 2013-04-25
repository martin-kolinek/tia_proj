package controllers

import play.api._
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

object OrderController extends Controller with ObjectController[OrderDesc] 
		with ObjectListController[OrderForList] {
	type ModelType = DBAccessConf with OrderModel with OrderList
	lazy val model = new DBAccessConf with Orders with DBPartDef with OrderModel with OrderList
	
	def pdefMapping(implicit s:scala.slick.session.Session) = mapping(
			"id" -> optional(number),
			"pdefid" -> number.verifying(model.existsPartDef _),
			"filter" -> nonEmptyText,
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

    def listRoute = routes.OrderController.list
	
	def listTemplate = views.html.order.list.apply

    def statusForm = Form(single(
        "statuses" -> play.api.data.Forms.list(mapping(
            "odefid" -> number,
            "parts" -> play.api.data.Forms.list(mapping(
                "part" -> number
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
            errFrm => {println(errFrm.errors);  BadRequest(views.html.order.status(errFrm, routes.OrderController.updateStatus(id)))},
            statuses => {
                model.withTransaction { implicit s =>
                    model.updateOrderStatus(id, statuses)
                    Redirect(listRoute)
                }
            }
        )
    }

    def listDefinitions(id:Int) = Action{
        model.withTransaction { implicit s =>
            Ok(views.html.order.definitions(model.listOrderDefs(id)))
        }
    }
}
