package models

import scala.slick.lifted.MappedTypeMapper
import models.enums._
import java.sql.Timestamp
import org.joda.time.DateTime

package object mappers {
	implicit val orderStatusTypeMapper = MappedTypeMapper.base[OrderStatusType, Int](_.id, OrderStatus(_))
    implicit val utilDateTypeMapper = MappedTypeMapper.base[DateTime, Timestamp](x=>new Timestamp(x.getMillis()), x => new DateTime(x.getTime()))
}