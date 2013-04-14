package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.DBAccess
import org.joda.time.DateTime
import models.WithID
import models.ObjectModel

case class PackDesc(heatNo:String, 
		deliveryDate:DateTime, 
		unlimited:Boolean, 
		material:MaterialDesc, 
		shape:ShapeDesc, 
		semiproducts:List[WithID[SemiproductDesc]]) 

case class SemiproductDesc(serialNo:String) {}

trait Semiproducts extends Shapes with Materials with ObjectModel[PackDesc] { this: DBAccess =>
    import profile.simple._
    
    private val packQuery = for {
    		shp@(shapeId, _, _, _, _, _, _) <- basicShapeJoin
    		pack <- Pack if pack.shapeId === shapeId
    		mat <- pack.material
        } yield (shp, pack, mat)
    
    def extractPackDesc(shp:OptionShape, pack:DBPack, mat:DBMaterial, semiprods:List[WithID[SemiproductDesc]]) = {
    	PackDesc(pack.heatNo, pack.deliveryDate, pack.unlimited, MaterialDesc(mat.name), extractShape(shp), semiprods)
    }
        
    /*def listPacks(implicit session:Session) = {
    	packQuery.list().map((extractPackDesc _).tupled)
    }*/
        
    def get(id:Int)(implicit session:Session) = {
    	val q = packQuery.filter(_._2.id === id)
    	for{
    		pck <- q.firstOption
    		semiprods = getPackSemiproducts(pck._2.id)
    	} yield extractPackDesc(pck._1, pck._2, pck._3, semiprods)
    }
    
    def getPackSemiproducts(packId:Int)(implicit session:Session) = {
    	Semiproduct.filter(_.packId===packId).map(x=>(x.id, x.serialNo)).list()
    			.map(x=>WithID(Some(x._1), SemiproductDesc(x._2)))
    }
    
    def insert(pck:PackDesc)(implicit session:Session) = {
    	val matId = getOrCreateMaterial(pck.material)
    	val shapeId = getOrCreateShapeId(pck.shape)
    	Pack.forInsert.insert(matId, pck.unlimited, shapeId, pck.deliveryDate, pck.heatNo)
    }
    
    def update(id:Int, pck:PackDesc)(implicit session:Session) {
    	val matId = getOrCreateMaterial(pck.material)
    	val shapeId = getOrCreateShapeId(pck.shape)
    	(for {
    		dbpck <- Pack if dbpck.id === id
    	} yield dbpck.materialId ~ dbpck.unlimited ~ dbpck.shapeId ~ dbpck.deliveryDate ~ dbpck.heatNo)
    	    .update((matId, pck.unlimited, shapeId, pck.deliveryDate, pck.heatNo))
        modifyPackSemiproducts(id, pck.semiproducts)
    }
    
    def modifyPackSemiproducts(packId:Int, sps:List[WithID[SemiproductDesc]])(implicit session:Session) {
    	val q = for {
    		sp <- Semiproduct if sp.packId === packId && !sp.id.inSet(sps.collect{case WithID(Some(id), _) => id})
    	} yield sp
    	q.delete
    	
    	for(sp <- sps.filter(_.id.isDefined)) {
    		Semiproduct.filter(_.id === sp.id.get).map(_.serialNo).update(sp.obj.serialNo)
    	}
    	
    	for(sp <- sps.filter(_.id.isEmpty)) {
    		Semiproduct.forInsert.insert((packId, sp.obj.serialNo))
    	}
    }
}
