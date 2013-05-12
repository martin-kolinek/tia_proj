package models

import scala.slick.lifted.Column
import scala.slick.lifted.BaseTypeMapper
import scala.slick.driver.BasicDriver.simple._

object SlickExtensions {
	implicit class OptionEqual[T : BaseTypeMapper](col : Column[Option[T]]) {
		def ==?(c2 : Column[Option[T]]) = col === c2 || col.isNull && c2.isNull
		def >=?<(c2 : Column[Option[T]]) = col >= c2 || col.isNull
		def <=?<(c2 : Column[Option[T]]) = col >= c2 || col.isNull
		def ==?<(c2 : Column[Option[T]]) = col === c2 || col.isNull
	}
}