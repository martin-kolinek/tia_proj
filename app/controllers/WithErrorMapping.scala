package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraint

class WithErrorMapping[T](under:Mapping[T], msg:String) extends Mapping[T] {
	def bind(data: Map[String, String]) = under.bind(data) match {
		case r@Right(x) => r
		case Left(errors) => Left(formErrors)
	}
	
	private def formErrors = Seq(new FormError(under.key, msg))
	
	val constraints = under.constraints
	
	val key = under.key
	
	val mappings = under.mappings
	
	def unbind(value:T) = {
		val (a, b) = under.unbind(value)
		(a, if(b.isEmpty) b else formErrors)
	}
	
	def verifying(constraints: Constraint[T]*) = under.verifying(constraints:_*)
	
	def withPrefix(prefix:String) = new WithErrorMapping(under.withPrefix(prefix), msg)
}