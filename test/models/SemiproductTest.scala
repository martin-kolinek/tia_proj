package models

import org.scalatest.FunSuite
import models.semiproduct.Semiproducts
import play.api.test._
import play.api.test.Helpers._
import Helpers._
import play.api.Play.current
import com.github.nscala_time.time.Imports._
import models.semiproduct.PackDesc
import models.semiproduct.ShapeDesc
import models.semiproduct.MaterialDesc
import models.semiproduct.CirclePipeDesc
import models.semiproduct.SheetDesc
import models.semiproduct.CirclePipeDesc
import models.semiproduct.CirclePipeDesc

class SemiproductTest extends FunSuite {
	test("listing and inserting of semiproducts works") {
		running(FakeApplication(additionalConfiguration = inMemorySlick)) {
			val sp = new DBAccessConf with Semiproducts
			import sp.profile.simple._
			val dt = new DateTime(2011, 11, 11, 11, 11, 11)
			sp.withSession{ implicit session => 
				val should = Set(
						PackDesc("heat", dt, false, MaterialDesc("material"), ShapeDesc),
						PackDesc("heat", dt+1.minute, true, MaterialDesc("material"), CirclePipeDesc(Some(9.0), Some(200.0), Some(20.0))),
						PackDesc("heat", dt, false, MaterialDesc("material"), SheetDesc(Some(10.0), None, None)))
				should.foreach(sp.insertPack(_))
				val pcks = sp.listPacks.toSet
				assert(should == pcks)
			}
		}
	}
}