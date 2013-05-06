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
            import tbls.profile.simple._
			assert(tbls.profile.isInstanceOf[scala.slick.driver.H2Driver])
		}
	}
	
	test("getTables function works with different configuration") {
		val r = running(FakeApplication(additionalConfiguration = inMemoryDatabase() ++ Map(SlickConf.slickDriverConfigOption->"scala.slick.driver.PostgresDriver"))) {
			val tbls = getTables
			assert(tbls.profile.isInstanceOf[scala.slick.driver.PostgresDriver])
			val ddl = tbls.CirclePipe.ddl ++ tbls.Cutting.ddl ++ tbls.CuttingPlan.ddl ++ tbls.ExtendedPipe.ddl ++ tbls.ExtendedSheet.ddl ++ tbls.CommonShape.ddl ++ tbls.Material.ddl ++ tbls.Order.ddl ++ tbls.Pack.ddl ++ tbls.Part.ddl ++ tbls.PartDefinition.ddl ++ tbls.PartDefinitionInCuttingPlan.ddl ++ tbls.OrderDefinition.ddl ++ tbls.Semiproduct.ddl ++ tbls.Shape.ddl ++ tbls.Sheet.ddl ++ tbls.SquarePipe.ddl
			/*ddl.createStatements.foreach(x=>info(x+";"))
			info("DROP")
			ddl.dropStatements.foreach(x=>info(x+";"))*/
		}
	}
}
