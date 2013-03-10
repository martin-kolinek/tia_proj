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

trait ShapeDesc {
	def description:String
}

object ShapeHelpers {
    implicit class OptionStringContext(sc:StringContext) {
        def opt(opts:Option[Any]*) = sc.s(opts.map(o => o.map(_.toString).getOrElse("?")):_*)
    }
}

import ShapeHelpers._

case object UnknownShapeDesc extends ShapeDesc {
    def description = "something"
}

case class SheetDesc(thickness:Option[BigDecimal], width:Option[BigDecimal], height:Option[BigDecimal]) extends ShapeDesc {
    def description = opt"sheet - $thickness mm, $width x $height"
}

case class CirclePipeDesc(thickness:Option[BigDecimal], radius:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
    def description = opt"circle pipe - $thickness, R=$radius, L=$length" 
} 

case class SquarePipeDesc(thickness:Option[BigDecimal], diameter:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
    def description = opt"square pipe - $thickness, D=$diameter, L=$length"
}

trait Shapes extends Tables { this:DBAccess =>
	import profile.simple._
	
	def optionSheet(s:Sheet.type) = s.id.? ~ s.thickness //<> (OptionSheet, OptionSheet.unapply _)
		
	def optionCirclePipe(c:CirclePipe.type) = c.id.? ~ c.radius ~ c.thickness //<> (OptionCirclePipe, OptionCirclePipe.unapply _)
				
	def optionSquarePipe(s:SquarePipe.type) = s.id.? ~ s.diameter ~ s.thickness //<> (OptionSquarePipe, OptionSquarePipe.unapply _)
		
	def optionExtendedSheet(s:ExtendedSheet.type) = s.id.? ~ s.width ~ s.height //<> (OptionExtendedSheet, OptionExtendedSheet.unapply _)
		
	def optionExtendedPipe(p:ExtendedPipe.type) = p.id.? ~ p.length //<> (OptionExtendedPipe, OptionExtendedPipe.unapply _)
		
    def basicShapeJoin = for {
        (((shape, sheet), circ), square) <- CommonShape leftJoin
            Sheet on (_.id === _.commonShapeId) leftJoin
            CirclePipe on (_._1.id === _.commonShapeId) leftJoin
            SquarePipe on (_._1._1.id === _.commonShapeId)
    } yield (shape.id, optionSheet(sheet), optionCirclePipe(circ), optionSquarePipe(square))

    def extendedShapeJoin = for {
        ((shape, extSheet), extPipe) <- CommonShape leftJoin
            ExtendedSheet on (_.id === _.commonShapeId) leftJoin
            ExtendedPipe on (_.id === _.commonShapeId)
    } yield (shape.id, optionExtendedSheet(extSheet), optionExtendedPipe(extPipe))

    def shapeQuery = for {
        shp <- Shape
        (bid, sheet, circ, square) <- basicShapeJoin if bid === shp.basicShapeId
        (eid, extSheet, extPipe) <- extendedShapeJoin if eid === shp.extendedShapeId
    } yield (shp, sheet, circ, square, extSheet, extPipe)

    type OptionShape = ((Int, Int, Option[Int]), 
                        OptionSheet, OptionCirclePipe, OptionSquarePipe, OptionExtendedSheet, OptionExtendedPipe)
    
    def extractShape: ((
    	case (_, (Some(_), thick), _, _, (_, width, height), _) => SheetDesc(thick, width, height)
    	case (_, _, (Some(_), thick, rad), _, _, (_, len)) => CirclePipeDesc(thick, rad, len)
    	case (_, _, _, (Some(_), thick, diam), _, (_, len)) => SquarePipeDesc(thick, diam, len)
    	case _ => UnknownShapeDesc
    }
    
    private val nextval = SimpleFunction.unary[String, Int]("nextval")
    
    private def insertEmptyCommonShape()(implicit session:Session) = CommonShape.forInsert.insert(Query(nextval("common_shape_id_seq"))).head
    
    private def optionEqualDec(c1:Column[Option[BigDecimal]], c2:Column[Option[BigDecimal]]) = c1 === c2 || (c1.isNull && c2.isNull)

    private def optionEqual[T](c1:Column[Option[T]], c2:Column[Option[T]]) = c1 === c2 || (c1.isNull && c2.isNull)
    
    private def getBasicShapeId(shp:ShapeDesc)(implicit s:Session) = {
        val q = for {
            (bid, (sheetId, sheetThick), (circId, circThick, circRad), (squareId, squareThick, squareDiam)) <- basicShapeJoin
            if(shp match {
                case SheetDesc(thick, _, _) => sheetId.isNotNull && optionEqualDec(thick, sheetThick)
                case CirclePipeDesc(thick, rad, _) => circId.isNotNull && optionEqualDec(thick, circThick) && optionEqualDec(rad, circRad)
                case SquarePipeDesc(thick, diam, _) => squareId.isNotNull && optionEqualDec(thick, squareThick) && optionEqualDec(diam, squareDiam)
            })
        } yield bid
        q.firstOption
    }

    private def getExtendedShapeId(shp:ShapeDesc)(implicit s:Session) = {
        val q = for {
            (eid, (sheetId, sheetWidth, sheetHeight), (pipeId, pipeLength)) <- extendedShapeId
            if(shp match {
                case SheetDesc(_, width, height) => sheetId.isNotNull && optionEqualDesc(width, sheetWidth), optionEqualDesc(height, sheetHeight)
                case CirclePipeDesc(_, _, len) | SquarePipeDesc(_, _, len) => pipeId.isNotNull && optionEqualDesc(len, pipeLength)
            })
        }
        q.firstOption
    }

    private def isExtended: ShapeDesc => Boolean = {
        case SheetDesc(_, None, None) | CirclePipeDesc(_, _, None) | SquarePipeDesc(_, _, None) => false
        case _ => true
    }

    private def getShapeId(basicId:Int, extendedId:Option[Int]) = {
        val q = for {
            shp <- Shape
            if shp.basicShapeId === basicId && 
        }
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
