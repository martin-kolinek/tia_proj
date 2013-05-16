package controllers

import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraint

class WithErrorMapping[T](under:Mapping[T], msg:String, val constraints:List[Constraint[T]] = Nil) extends Mapping[T] {
	def bind(data: Map[String, String]) = 
        under.bind(data) match {
		    case Right(x) => applyConstraints(x)
		    case Left(errors) => Left(formErrors)
        }

	private def formErrors = Seq(new FormError(under.key, msg))
	
	val key = under.key
	
	val mappings = under.mappings
	
	def unbind(value:T) = {
		val (a, b) = under.unbind(value)
		(a, if(b.isEmpty) b else formErrors)
	}
	
	def verifying(cstrs: Constraint[T]*) = new WithErrorMapping[T](under, msg, constraints++cstrs)
	
	def withPrefix(prefix:String) = new WithErrorMapping(under.withPrefix(prefix), msg)
}
