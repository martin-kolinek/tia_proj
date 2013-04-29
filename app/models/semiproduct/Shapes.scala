package models.semiproduct

import models.basic.Tables
import models.DBAccess

object ShapeOptionTypes {
	type OptionSheet = (Option[Int], Option[BigDecimal])
	type OptionCirclePipe = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionSquarePipe = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionExtendedSheet = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionExtendedPipe = (Option[Int], Option[BigDecimal])
}

import ShapeOptionTypes._

class ShapeDesc protected() {
	def description = "something" 
}

object ShapeDesc extends ShapeDesc {
}

object ShapeHelpers {
    implicit class OptionStringContext(sc:StringContext) {
        def opt(opts:Option[Any]*) = sc.s(opts.map(o => o.map(_.toString).getOrElse("?")):_*)
    }
}

import ShapeHelpers._

case class SheetDesc(thickness:Option[BigDecimal], width:Option[BigDecimal], height:Option[BigDecimal]) extends ShapeDesc {
	override def description = opt"sheet - $thickness mm, $width x $height"
}

case class CirclePipeDesc(thickness:Option[BigDecimal], radius:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
	override def description = opt"circle pipe - $thickness, R=$radius, L=$length" 
} 

case class SquarePipeDesc(thickness:Option[BigDecimal], diameter:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
	override def description = opt"square pipe - $thickness, D=$diameter, L=$length"
}

trait Shapes extends Tables { this:DBAccess =>
	import profile.simple._
	
	def optionSheet(s:Sheet.type) = s.id.? ~ s.thickness //<> (OptionSheet, OptionSheet.unapply _)
		
	def optionCirclePipe(c:CirclePipe.type) = c.id.? ~ c.radius ~ c.thickness //<> (OptionCirclePipe, OptionCirclePipe.unapply _)
				
	def optionSquarePipe(s:SquarePipe.type) = s.id.? ~ s.diameter ~ s.thickness //<> (OptionSquarePipe, OptionSquarePipe.unapply _)
		
	def optionExtendedSheet(s:ExtendedSheet.type) = s.id.? ~ s.width ~ s.height //<> (OptionExtendedSheet, OptionExtendedSheet.unapply _)
		
	def optionExtendedCirclePipe(p:ExtendedCirclePipe.type) = p.id.? ~ p.length //<> (OptionExtendedPipe, OptionExtendedPipe.unapply _)
		
	def optionExtendedSquarePipe(p:ExtendedSquarePipe.type) = p.id.? ~ p.length //<> (OptionExtendedPipe, OptionExtendedPipe.unapply _)
		
    def basicShapeJoin = for{
    	((((((shape, sheet), circ), square), extSheet), extCirc), extSquare) <- Shape leftJoin 
    	    Sheet on (_.id === _.shapeId) leftJoin 
    	    CirclePipe on (_._1.id === _.shapeId) leftJoin
    	    SquarePipe on (_._1._1.id === _.shapeId) leftJoin
    	    ExtendedSheet on (_._1._1._2.id === _.sheetId) leftJoin
    	    ExtendedCirclePipe on (_._1._1._2.id === _.circlePipeId) leftJoin
    	    ExtendedSquarePipe on (_._1._1._2.id === _.squarePipeId)
    } yield (shape.id, optionSheet(sheet), optionCirclePipe(circ), optionSquarePipe(square), optionExtendedSheet(extSheet), optionExtendedCirclePipe(extCirc), optionExtendedSquarePipe(extSquare))

    type OptionShape = (Int, OptionSheet, OptionCirclePipe, OptionSquarePipe, OptionExtendedSheet, OptionExtendedPipe, OptionExtendedPipe)
    
    def extractShape: OptionShape => ShapeDesc = {
    	case (_, (Some(_), thick), _, _, (_, width, height), _, _) => SheetDesc(thick, width, height)
    	case (_, _, (Some(_), rad, thick), _, _, (_, len), _) => CirclePipeDesc(thick, rad, len)
    	case (_, _, _, (Some(_), diam, thick), _, _, (_, len)) => SquarePipeDesc(thick, diam, len)
    	case _ => ShapeDesc
    }
    
    private val nextval = SimpleFunction.unary[String, Int]("nextval")
    
    private def insertEmptyShape()(implicit session:Session) = Shape.forInsert.insert(Query(nextval("shape_id_seq"))).head
    
    private def optionEqualDec(c1:Column[Option[BigDecimal]], c2:Column[Option[BigDecimal]]) = c1 === c2 || (c1.isNull && c2.isNull)
    
    private def getShapeId(shp:ShapeDesc)(implicit session:Session) = {
    	val q = for {
    		(id, sheet, circ, square, extSheet, extCirc, extSquare) <- basicShapeJoin
    		if (shp match {
    			case SheetDesc(thick, None, None) => sheet._1.isNotNull && extSheet._1.isNull &&
    					optionEqualDec(thick, sheet._2)
    			case SheetDesc(t, w, h) => optionEqualDec(t, sheet._2) &&
    			        optionEqualDec(w, extSheet._2) && optionEqualDec(h, extSheet._3)
    			        
    			case CirclePipeDesc(thick, rad, None) => circ._1.isNotNull && extCirc._1.isNull &&
    			        optionEqualDec(thick, circ._3) && optionEqualDec(rad, circ._2)
    			case CirclePipeDesc(thick, rad, len) => optionEqualDec(thick, circ._3) &&
    			        optionEqualDec(rad, circ._2) && optionEqualDec(len, extCirc._2)
    			
    			case SquarePipeDesc(thick, diam, None) => square._1.isNotNull && extSquare._1.isNull &&
    			        optionEqualDec(thick, square._3) && optionEqualDec(diam, square._2)
    			case SquarePipeDesc(thick, diam, len) => optionEqualDec(thick, square._3) &&
    			        optionEqualDec(diam, square._2) && optionEqualDec(len, extSquare._2)
    			case _ => sheet._1.isNull && circ._1.isNull && square._1.isNull.?
    		})
    	} yield id
    	q.firstOption
    }
    
    private def insertShape(shp:ShapeDesc)(implicit session:Session) = {
    	val shpId = insertEmptyShape()
    	val basicId = shp match {
    		case SheetDesc(thick, _, _) => Sheet.forInsert.insert(shpId, thick)
    		case CirclePipeDesc(thick, rad, _) => CirclePipe.forInsert.insert(shpId, thick, rad)
    		case SquarePipeDesc(thick, diam, _) => SquarePipe.forInsert.insert(shpId, thick, diam)
    		case _ => 0
    	}
    	shp match {
    		case SheetDesc(_, width, height) => ExtendedSheet.forInsert.insert(basicId, width, height)
    		case CirclePipeDesc(_, _, len) => ExtendedCirclePipe.forInsert.insert(basicId, len)
    		case SquarePipeDesc(_, _, len) => ExtendedSquarePipe.forInsert.insert(basicId, len)
    		case _ => 
    	}
    	shpId
    }
    
    def getOrCreateShapeId(shp:ShapeDesc)(implicit session:Session) = {
    	getShapeId(shp).getOrElse(insertShape(shp))
    }
}
