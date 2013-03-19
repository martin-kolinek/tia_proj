package model.basic

import scala.slick.driver.ExtendedProfile
import model._

trait WithProfile {
    val profile:ExtendedProfile
}

trait Tables { this:WithProfile =>
    import profile.simple._
    
    object Order extends Table[(Int, String, DateTime, Option[DateTime], OrderStatus)]("orders") {
        def id = column[Int]("id")
        def name = column[String]("name")
        def fillingDate = column[DateTime]("filling_date")
        def dueDate = column[Option[DateTime]]("due_date")
        def status = column[OrderStatus]("status")
        def * = id ~ name ~ fillingDate ~ dueDate ~ status
    }
}
