package models

import scala.util.parsing.combinator.Parsers
import scala.util.parsing.combinator.RegexParsers

trait ValidationParser {
	this:RegexParsers =>
	def toValidation[T](res:ParseResult[T]) = res match {
		case Success(r, _) => scalaz.Success(r)
		case ns:NoSuccess => scalaz.Failure(ns.msg)
	}
}