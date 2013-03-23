package models

import play.api.Application
import scala.slick.driver.ExtendedProfile

package object basic {
	def getTables(implicit app:Application) = new DBAccessConf with Tables 
}