package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.DBAccess
import org.joda.time.DateTime

case class PackDesc(heatNo:String, deliveryDate:DateTime, unlimited:Boolean, material:MaterialDesc, shape:ShapeDesc) {	
}

case class SemiproductDesc(id:Int, serialNo:String) {} 

trait Semiproducts extends Shapes { this: DBAccess =>
    import profile.simple._
    
    private val packQuery = for {
    		shp@(shapeId, _, _, _, _, _, _) <- basicShapeJoin
    		pack <- Pack if pack.shapeId === shapeId
    		mat <- pack.material
        } yield (shp, pack, mat)
    
    def extractPackDesc(shp:OptionShape, pack:DBPack, mat:DBMaterial) = {
    	PackDesc(pack.heatNo, pack.deliveryDate, pack.unlimited, MaterialDesc(mat.name), extractShape(shp))
    }
        
    def listPacks(implicit session:Session) = {
    	packQuery.list().map((extractPackDesc _).tupled)
    }
        
    def packDetails(id:Int)(implicit session:Session) = {
    	val q = packQuery.filter(_._2.id === id)
    	val pack = q.firstOption().map((extractPackDesc _).tupled)
    	val sps = Semiproduct.filter(_.packId===id).map(x=>(x.id, x.serialNo)).list().map(SemiproductDesc.tupled)
    	pack.map(x=>(x, sps))
    }
    
    def insertPack(pck:PackDesc)(implicit session:Session) {
    	
    }
    
}
