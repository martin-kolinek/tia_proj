package models

import play.api.Application
import scala.slick.driver.ExtendedProfile

package object basic {
	def getTables(implicit app:Application) = {
		val driver = app.configuration.getString(SlickConf.slickDriverConfigOption).getOrElse(SlickConf.defaultSlickDriver)
		val ru = scala.reflect.runtime.universe
		val mir = ru.runtimeMirror(app.classloader)
		val driverSymbol = mir.staticModule(driver)
		new Tables() with WithProfile { 
			lazy val profile = mir.reflectModule(driverSymbol).instance.asInstanceOf[ExtendedProfile]
		}
	}
}