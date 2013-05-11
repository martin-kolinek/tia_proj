package controllers

import models.semiproduct.SemiproductFilter
import play.api.data.validation.Constraint
import scalaz.Success
import play.api.data.validation.Valid
import play.api.data.validation.Invalid
import scalaz.Failure

object SemiproductFilterHelpers {
	def filterConstraint(model:SemiproductFilter) = 
			Constraint{str:String => 
			model.parseSemiproductFitler(str) match {
				case Success(_) => Valid
				case Failure(msg) => Invalid(msg)
			}
	}
}