package views

import views.html.helper.FieldConstructor

object HorizontalForm {
	implicit val fconst = FieldConstructor(views.html.horizontalFormFieldConstructor.f)
}