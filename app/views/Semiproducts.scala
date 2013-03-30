package views

import models.semiproduct.PackDesc
import play.api.templates.Html
import models.semiproduct.SemiproductDesc
import models.semiproduct.SemiproductDesc
import play.api._
import play.api.mvc._
import Helpers._

object Semiproducts {
	implicit def packDisplayable(implicit request:RequestHeader) = new KeyValDisplayable[PackDesc] {
		def header: Seq[play.api.templates.Html] = Seq(
				Html("Heat no"),
				Html("Delivery date"),
				Html("Real?"),
				Html("Material"),
				Html("Shape"))
		def row(t: models.semiproduct.PackDesc): Seq[play.api.templates.Html] = Seq(
				Html(t.heatNo),
				Html(t.deliveryDate.toString),
				Html(t.unlimited.toString),
				Html(t.material.name),
				Html(t.shape.description)
				)
	}
	implicit val spDisplayable = new KeyValDisplayable[SemiproductDesc] {
		def header = Seq(Html("serial no"))
		def row(t:SemiproductDesc) = Seq(Html(t.serialNo))
	}
}