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
			sp.withTransaction{ implicit session => 
				val should = Set(
						PackDesc("heat", dt, false, MaterialDesc("material"), ShapeDesc, Nil),
						PackDesc("heat", dt+1.minute, true, MaterialDesc("material"), CirclePipeDesc(Some(9.0), Some(200.0), Some(20.0)), Nil),
						PackDesc("heat", dt, false, MaterialDesc("material"), SheetDesc(Some(10.0), None, None), Nil))
				should.foreach(sp.insert(_))
				/*val pcks = sp.list.map(_.obj).toSet
				assert(should == pcks)*/
			}
		}
	}
	
	test("updating of semiproducts works") {
		running(FakeApplication(additionalConfiguration = inMemorySlick)) {
			val sp = new DBAccessConf with Semiproducts
			import sp.profile.simple._
			val dt = new DateTime(2011, 11, 11, 11, 11, 11)
			sp.withTransaction{ implicit session => 
				val old = PackDesc("heat", dt, false, MaterialDesc("material"), ShapeDesc, Nil)
				val id = sp.insert(old)
				val should = PackDesc("heat2", dt+2.seconds, true, MaterialDesc("material2"), SheetDesc(Some(10.0), None, None), Nil)
				sp.update(id, should)
				/*val pcks = sp.listPacks.map(_.obj).head
				assert(should == pcks)*/
			}
		}
	}
}