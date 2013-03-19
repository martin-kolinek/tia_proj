package models

import scala.slick.driver.BasicDriver.simple._
import scala.slick.lifted.TypeMapper
import scala.slick.lifted.MappedTypeMapper.base

package object Package {
    type OrderStatus = OrderStatus.Value
    implicit val orderStatusTypeMapper = MappedTypeMapper.base[OrderStatus, Int](_.id, OrderStatus(_))
}
