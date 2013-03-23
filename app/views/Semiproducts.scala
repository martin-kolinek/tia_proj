package views

import models.semiproduct.PackDesc
import play.api.templates.Html

object Semiproducts {
	implicit val packDisplayable = new GridDisplayable[PackDesc] {
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
}