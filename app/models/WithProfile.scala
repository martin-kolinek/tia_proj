package models

import slick.session.Database
import play.api.Application
import scala.slick.driver.ExtendedProfile

trait WithProfile {
    val profile:ExtendedProfile
}

