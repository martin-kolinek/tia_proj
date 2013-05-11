package models.partdef

import models._
import models.basic._
import models.semiproduct.Shapes
import models.semiproduct.MaterialDesc
import models.semiproduct.ShapeDesc
import models.semiproduct.SemiproductFilter
import models.semiproduct.ShapeFilter
import models.semiproduct.Semiproducts
import scalaz.Success
import scalaz.Success

case class PartDefinitionDesc(name:String, filter:String, file:Array[Byte])

case class PartDefinitionForList(id:Int, name:String, filter:String) {
	def description = s"$name ($filter)"
}

case class CutPart(shapeId:Int, shape:ShapeDesc, materialId:Int, material:MaterialDesc, count:Int)

trait PartDefinitions extends Tables {
	this:DBAccess with Shapes with Semiproducts with ShapeFilter with SemiproductFilter =>
	import profile.simple._
	
    private def idQuery(id:Int) = for(pd<-PartDefinition if pd.id === id) yield (pd.name, pd.filter, pd.file)

	def getPartDef(id:Int)(implicit session:Session) = {
		val q = idQuery(id)
		q.firstOption.map(PartDefinitionDesc.tupled)
	}

    def existsPartDef(id:Int)(implicit session:Session) = {
        idQuery(id).firstOption.isDefined
    }
	
	def listPartDefinitions(implicit session:Session) = {
		val q = for(pd<-PartDefinition) yield (pd.id, (pd.name, pd.filter, pd.file))
		q.list.map(x=>WithID(Some(x._1), PartDefinitionDesc.tupled(x._2)))
	}
	
	def updatePartDef(id:Int, pd:PartDefinitionDesc)(implicit session:Session) {
		val q = for {
			dpd <- PartDefinition if dpd.id === id
		} yield dpd.file ~ dpd.filter ~ dpd.name
		q.update(pd.file, pd.filter, pd.name)
	}
	
	def hidePartDefinition(id:Int)(implicit session:Session) {
		Query(PartDefinition).filter(_.id === id).map(_.hidden).update(true)
	}
	
	def insertPartDef(pd:PartDefinitionDesc)(implicit session:Session) = {
		PartDefinition.forInsert.insert((pd.file, pd.filter, pd.name, false))
	}

	def getPartDefForList(id:Int)(implicit session:Session) = {
		Query(PartDefinition).filter(_.id === id).map(x=>(x.id, x.name, x.filter)).firstOption.
		    map(PartDefinitionForList.tupled)
	}
	
	def listFinishedParts(forOrderDef:Int)(implicit s:Session) = {
		val filt = Query(OrderDefinition).filter(_.id === forOrderDef).map(_.filter).firstOption
		val filter = filt.map(parseSemiproductFitler) match {
			case Some(scalaz.Success(f)) => f
			case _ => Nil
		}
		val join = for {
			p <- Part if p.orderDefId.isNull
			c <- p.cutting
			sp <- c.semiproduct
			pck <- sp.pack
			shp <- pck.shape
			pck2 <- (packQuery /: filter)(_.filter(_)) if pck2._2.id === pck.id
		} yield (shp, pck)
		val grouped =  join.groupBy(x=>(x._1.basicShapeId, x._2.materialId)).map {
			case ((shpid, matid), rows) => (shpid, matid, rows.length)
		}
		val lst = grouped.list
		
		val q = for {
			(shpid, matid, cnt) <- grouped
			shp <- basicShapeJoin if shp._1 === shpid
			mat <- Material if mat.id === matid
		} yield (shp, mat, cnt)
		
		q.list.map {
		    case (shp, mat, cnt) => CutPart(shp._1, extractBasicShape(shp), 0, MaterialDesc(mat.name), cnt)
		}
	}
}
