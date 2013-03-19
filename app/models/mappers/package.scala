package models

import scala.slick.lifted.MappedTypeMapper
import models.enums._
import java.util.Date
import java.sql.{Date => SqlDate}

package object mappers {
	implicit val orderStatusTypeMapper = MappedTypeMapper.base[OrderStatusType, Int](_.id, OrderStatus(_))
    implicit val utilDateTypeMapper = MappedTypeMapper.base[Date, SqlDate](x=>new SqlDate((x).getTime()), identity)
}