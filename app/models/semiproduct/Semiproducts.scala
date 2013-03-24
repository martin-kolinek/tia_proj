package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.DBAccess
import org.joda.time.DateTime

case class PackDesc(id:Int, heatNo:String, deliveryDate:DateTime, unlimited:Boolean, material:MaterialDesc, shape:ShapeDesc) {	
}

trait Semiproducts extends Shapes { this: DBAccess =>
    import profile.simple._
    
    def listPacks(implicit session:Session) = {
    	val q = for{
    		shp@(shapeId, _, _, _, _, _, _) <- basicShapeJoin
    		pack <- Pack if pack.shapeId === shapeId
    		mat <- pack.material
    	} yield (shp, pack.id, pack.unlimited, pack.deliveryDate, pack.heatNo, mat.name)
    	q.list().map{
    		case (shp, id, unlim, deliv, heat, mat) => PackDesc(id, heat, deliv, unlim, MaterialDesc(mat), extractShape.tupled(shp))
    	}
    }
}
