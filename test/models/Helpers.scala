package models

import play.api.test.Helpers._

object Helpers {
	def inMemorySlick = inMemoryDatabase() +  
		(SlickConf.slickDriverConfigOption->SlickConf.defaultSlickDriver)
}