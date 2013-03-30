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

class SemiproductTest extends FunSuite {
	test("listing of semiproducts works") {
		running(FakeApplication(additionalConfiguration = inMemorySlick)) {
			val sp = new DBAccessConf with Semiproducts
			import sp.profile.simple._
			val dt = new DateTime(2011, 11, 11, 11, 11, 11)
			sp.withSession{ implicit session => 
				/*val sh = sp.insertShape
				val sh2 = sp.insertShape
				val sh3 = sp.insertShape
				val sheet = (sp.Sheet.shapeId ~ sp.Sheet.thickness returning sp.Sheet.id).insert(sh2, Some(10.0))
				val circ = (sp.CirclePipe.shapeId ~ sp.CirclePipe.thickness ~ sp.CirclePipe.radius returning sp.CirclePipe.id).insert(sh3, Some(9.0), Some(200.0))
				val extCirc = (sp.ExtendedCirclePipe.circlePipeId ~ sp.ExtendedCirclePipe.length returning sp.ExtendedCirclePipe.id).insert(circ, Some(20.0))
				val mat = sp.Material.name.insert("material")
				(sp.Pack.id ~
					sp.Pack.heatNo ~ 
					sp.Pack.deliveryDate ~ 
					sp.Pack.materialId ~ 
					sp.Pack.shapeId ~ 
					sp.Pack.unlimited).insertAll(
							(1, "heat", dt, mat, sh, false),
							(2, "heat", dt+1.minute, mat, sh3, true),
							(3, "heat", dt, mat, sh2, false))
				val pcks = sp.listPacks.toSet
				val should = Set(
						PackDesc("heat", dt, false, MaterialDesc("material"), ShapeDesc),
						PackDesc("heat", dt+1.minute, true, MaterialDesc("material"), CirclePipeDesc(Some(9.0), Some(200.0), Some(20.0))),
						PackDesc("heat", dt, false, MaterialDesc("material"), SheetDesc(Some(10.0), None, None)))
				assert(should == pcks)*/
			}
		}
	}
}