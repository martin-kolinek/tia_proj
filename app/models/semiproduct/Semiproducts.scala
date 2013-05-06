package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.DBAccess
import org.joda.time.DateTime
import models.WithID
import models.ObjectModel
import models.enums._

case class PackDesc(heatNo:String, 
		deliveryDate:DateTime, 
		unlimited:Boolean, 
		material:MaterialDesc, 
		shape:ShapeDesc, 
		semiproducts:List[WithID[SemiproductDesc]]) 

case class SemiproductDesc(serialNo:String) {}

case class PackForList(id:Int, heatNo:String, deliveryDate:DateTime, unlimited:Boolean, material:MaterialDesc, shape:ShapeDesc) {
    def description = s"${material.name} ${shape.description} $heatNo"
}

case class SemiproductForList(id:Int, serialNo:String, status:SemiproductStatusType)

trait Semiproducts extends Shapes with Materials { this: DBAccess =>
    import profile.simple._
    
    val packQuery = for {
    		shp <- shapeQuery
    		pack <- Pack if pack.shapeId === shp._1.id
    		mat <- pack.material
        } yield (shp, pack, mat)
        
    def extractPackDesc(shp:OptionShape, pack:DBPack, mat:DBMaterial, semiprods:List[WithID[SemiproductDesc]]) = {
    	PackDesc(pack.heatNo, pack.deliveryDate, pack.unlimited, MaterialDesc(mat.name), extractShape(shp), semiprods)
    }
        
    def extractPackForList(shp:OptionShape, pck:DBPack, mat:DBMaterial) = 
        PackForList(pck.id, pck.heatNo, pck.deliveryDate, pck.unlimited, MaterialDesc(mat.name), extractShape(shp))
        
    def getPack(id:Int)(implicit session:Session) = {
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
    
    def insertPack(pck:PackDesc)(implicit session:Session) = {
    	val matId = getOrCreateMaterial(pck.material)
    	val shapeId = getOrCreateShapeId(pck.shape)
    	val ret = Pack.forInsert.insert(matId, pck.unlimited, shapeId, pck.deliveryDate, pck.heatNo)
    	modifyPackSemiproducts(ret, pck.semiproducts)
    	ret
    }
    
    def updatePack(id:Int, pck:PackDesc)(implicit session:Session) {
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

    def existsSemiproduct(id:Int)(implicit s:Session) = Query(Semiproduct).filter(_.id === id).firstOption.isDefined
    
    def getSemiproductDescription(id:Int)(implicit s:Session) = {
    	val q = for {
    		sp <- Semiproduct if sp.id === id
    		value@(shp, pck, mat) <- packQuery if pck.id === sp.packId
    	} yield (value, sp.serialNo)
    	q.firstOption.map {
    		case (pck, serial) => (extractPackForList _).tupled(pck).description + " " + serial
    	}
    }
}
