package models

import models.basic._
import play.api.test._
import play.api.test.Helpers._
import play.api.Play.current
import org.specs2.mutable._
import org.scalatest.FunSuite
import Helpers._

class BasicModelsTest extends FunSuite {
	test("getTables function works") {
		val r = running(FakeApplication(additionalConfiguration = inMemorySlick)) {
			val tbls = getTables
			assert(tbls.profile.isInstanceOf[scala.slick.driver.H2Driver])
		}
	}
	
	test("getTables function works with different configuration") {
		val r = running(FakeApplication(additionalConfiguration = Map(SlickConf.slickDriverConfigOption->"scala.slick.driver.SQLiteDriver"))) {
			val tbls = getTables
			assert(tbls.profile.isInstanceOf[scala.slick.driver.SQLiteDriver])
		}
	}
}