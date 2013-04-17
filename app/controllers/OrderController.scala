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
import models.order.PartDefInOrder
import views.html.order.order_form
import models.order.OrderModel
import models.order.OrderForList
import models.order.OrderList

object OrderController extends Controller with ObjectController[OrderDesc] 
		with ObjectListController[OrderForList] {
	type ModelType = DBAccessConf with OrderModel with OrderList
	lazy val model = new DBAccessConf with Orders with DBPartDef with OrderModel with OrderList
	
	def pdefMapping(implicit s:scala.slick.session.Session) = mapping(
			"id" -> number.verifying(model.existsPartDef _),
			"filter" -> nonEmptyText,
			"count" -> number(1))(PartDefInOrder)(PartDefInOrder.unapply)
	
	def form(implicit s:scala.slick.session.Session) = Form(mapping(
			"name" -> nonEmptyText,
			"filling_date" -> jodaDate,
			"due_date" -> optional(jodaDate),
			"status" -> number.
			    verifying("status out of bounds", OrderStatus.values.map(_.id).contains _).
			    transform(OrderStatus.apply, (x:OrderStatusType) => x.id),
			"pdefs" -> play.api.data.Forms.list(pdefMapping))
			(OrderDesc)(OrderDesc.unapply))
			
	def template = order_form.apply
	
	def saveRoute = routes.OrderController.save
	
	def updateRoute = routes.OrderController.update _
	
	def listTemplate = views.html.order.list.apply
}