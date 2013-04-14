package models

import play.api.Application
import scala.slick.driver.ExtendedProfile
import play.api.db.DB
import scala.slick.lifted.OptionMapper2

trait DBAccess {
    val profile:ExtendedProfile
    import profile.simple._
    def withTransaction[T](act: Session => T):T
}

class DBAccessConf(implicit app:Application) extends DBAccess {
	private val driver = app.configuration.getString(SlickConf.slickDriverConfigOption).getOrElse(SlickConf.defaultSlickDriver)
	private val ru = scala.reflect.runtime.universe
	private val mir = ru.runtimeMirror(app.classloader)
	private val driverSymbol = mir.staticModule(driver)
	lazy val profile = mir.reflectModule(driverSymbol).instance.asInstanceOf[ExtendedProfile]
	import profile.simple._
	private val db = Database.forDataSource(DB.getDataSource())
	def withTransaction[T](act:Session => T) = db.withTransaction(act)
}