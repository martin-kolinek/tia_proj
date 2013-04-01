package models

case class WithID[T](id:Option[Int], obj:T) {}

