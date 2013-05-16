package controllers

import language.implicitConversions
import play.api.data.Mapping

object CustomConstraints {
	implicit class MappingWithErrorOps[T](m:Mapping[T]) {
		def withError(msg:String) = new WithErrorMapping[T](m, msg)
	}  
}