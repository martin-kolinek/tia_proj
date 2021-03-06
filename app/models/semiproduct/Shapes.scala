package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.SlickExtensions._
import slick.lifted.Projection2
import slick.lifted.Projection3

object ShapeOptionTypes {
	type OptionSheet = (Option[Int], Option[BigDecimal])
	type OptionCirclePipe = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionSquarePipe = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionExtendedSheet = (Option[Int], Option[BigDecimal], Option[BigDecimal])
	type OptionExtendedPipe = (Option[Int], Option[BigDecimal])
}

import ShapeOptionTypes._

sealed trait ShapeDesc {
	def description:String
}

object ShapeHelpers {
    implicit class OptionStringContext(sc:StringContext) {
        def opt(opts:Option[Any]*) = sc.s(opts.map(o => o.map(_.toString).getOrElse("?")):_*)
    }
    
    def pipeExtDesc(len:Option[BigDecimal]) = len match {
    	case None => ""
    	case Some(l) => s", L=$l mm"
    } 
    	
}

import ShapeHelpers._

case object UnknownShapeDesc extends ShapeDesc {
    def description = "something"
}

case class SheetDesc(thickness:Option[BigDecimal], width:Option[BigDecimal], height:Option[BigDecimal]) extends ShapeDesc {
	def extDesc = 
		if(width.isEmpty && height.isEmpty)
			""
		else
			opt", $width x $height mm"
	
    def description = opt"sheet - $thickness mm" + extDesc
}

case class CirclePipeDesc(thickness:Option[BigDecimal], radius:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
    def description = opt"circle pipe - $thickness mm, R=$radius mm" + ShapeHelpers.pipeExtDesc(length) 
} 

case class SquarePipeDesc(thickness:Option[BigDecimal], diameter:Option[BigDecimal], length:Option[BigDecimal]) extends ShapeDesc {
    def description = opt"square pipe - $thickness mm, D=$diameter mm" + ShapeHelpers.pipeExtDesc(length)
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
            ExtendedPipe on (_._1.id === _.commonShapeId)
    } yield (shape.id, optionExtendedSheet(extSheet), optionExtendedPipe(extPipe))

    def shapeQuery = for {
        shp <- Shape
        (bid, sheet, circ, square) <- basicShapeJoin if bid === shp.basicShapeId
        (eid, extSheet, extPipe) <- extendedShapeJoin if eid === shp.extendedShapeId
    } yield (shp, sheet, circ, square, extSheet, extPipe)

    type ShapeFilterInput = (Shape.type, Projection2[Option[Int],Option[BigDecimal]], Projection3[Option[Int],Option[BigDecimal],Option[BigDecimal]], Projection3[Option[Int],Option[BigDecimal],Option[BigDecimal]], Projection3[Option[Int],Option[BigDecimal],Option[BigDecimal]], Projection2[Option[Int],Option[BigDecimal]])

    type OptionShape = ((Int, Int, Int), 
                        OptionSheet, OptionCirclePipe, OptionSquarePipe, OptionExtendedSheet, OptionExtendedPipe)
    
    type OptionBasicShape = (Int, OptionSheet, OptionCirclePipe, OptionSquarePipe)
                        
    def extractShape: OptionShape => ShapeDesc = {
    	case (_, (Some(_), thick), _, _, (_, width, height), _) => SheetDesc(thick, width, height)
    	case (_, _, (Some(_), thick, rad), _, _, (_, len)) => CirclePipeDesc(thick, rad, len)
    	case (_, _, _, (Some(_), thick, diam), _, (_, len)) => SquarePipeDesc(thick, diam, len)
    	case _ => UnknownShapeDesc
    }
    
    def extractBasicShape: OptionBasicShape => ShapeDesc = {
    	case (_, (Some(_), thick), _, _) => SheetDesc(thick, None, None)
    	case (_, _, (Some(_), thick, rad), _) => CirclePipeDesc(thick, rad, None)
    	case (_, _, _, (Some(_), thick, diam)) => SquarePipeDesc(thick, diam, None)
        case _ => UnknownShapeDesc
    } 
    
    private val nextval = SimpleFunction.unary[String, Int]("nextval")
    
    private def insertEmptyCommonShape()(implicit session:Session) = CommonShape.forInsert.insert(Query(nextval("common_shape_id_seq"))).head
    
    private def getBasicShapeId(shp:ShapeDesc)(implicit s:Session) = {
        val q = for {
            (bid, (sheetId ~ sheetThick), (circId ~ circThick ~ circRad), (squareId ~ squareThick ~ squareDiam)) <- basicShapeJoin
            if(shp match {
                case SheetDesc(thick, _, _) => sheetId.isNotNull && sheetThick ==? thick
                case CirclePipeDesc(thick, rad, _) => circId.isNotNull && circThick ==? thick && circRad ==? rad
                case SquarePipeDesc(thick, diam, _) => squareId.isNotNull && squareThick ==? thick && squareDiam ==? diam
                case UnknownShapeDesc => squareId.isNull.? && sheetId.isNull && circId.isNull
            })
        } yield bid
        q.firstOption
    }

    private def getExtendedShapeId(shp:ShapeDesc)(implicit s:Session) = {
        val q = for {
            (eid, (sheetId ~ sheetWidth ~ sheetHeight), (pipeId ~ pipeLength)) <- extendedShapeJoin
            if(shp match {
                case SheetDesc(_, width, height) => sheetId.isNotNull && sheetWidth ==? width && sheetHeight ==? height
                case CirclePipeDesc(_, _, len) => pipeId.isNotNull && pipeLength ==? len
                case SquarePipeDesc(_, _, len) => pipeId.isNotNull && pipeLength ==? len
                case _ => true.?
            })
        } yield eid
        q.firstOption
    }

    private def getShapeId(basicId:Int, extendedId:Int)(implicit s:Session) = {
        val q = for {
            shp <- Shape
            if shp.basicShapeId === basicId && shp.extendedShapeId === extendedId
        } yield shp.id
        q.firstOption
    }

    private def insertBasicShape(shp:ShapeDesc)(implicit session:Session) = {
    	val commonId = insertEmptyCommonShape()
    	shp match {
    		case SheetDesc(thick, _, _) => Sheet.forInsert.insert(commonId, thick)
    		case CirclePipeDesc(thick, rad, _) => CirclePipe.forInsert.insert(commonId, thick, rad)
    		case SquarePipeDesc(thick, diam, _) => SquarePipe.forInsert.insert(commonId, thick, diam)
    		case _ =>
    	}
    	commonId
    }
    
    private def insertExtendedShape(shp:ShapeDesc)(implicit s:Session) = {
    	val commonId = insertEmptyCommonShape()
    	shp match {
    		case SheetDesc(_, width, height) => ExtendedSheet.forInsert.insert(commonId, width, height)
    		case CirclePipeDesc(_, _, len) => ExtendedPipe.forInsert.insert(commonId, len)
    		case SquarePipeDesc(_, _, len) => ExtendedPipe.forInsert.insert(commonId, len)
    		case _ =>
    	}
    	commonId
    }
    
    private def insertShape(basicId:Int, extId:Int)(implicit session:Session) = {
        Shape.forInsert.insert(basicId, extId)
    }
    
    def getOrCreateShapeId(shp:ShapeDesc)(implicit session:Session) = {
    	val basicId = getBasicShapeId(shp).getOrElse(insertBasicShape(shp))
    	val extId = getExtendedShapeId(shp).getOrElse(insertExtendedShape(shp))
    	getShapeId(basicId, extId).getOrElse(insertShape(basicId, extId))
    }
    
    def getBasicShapeDescription(id:Int)(implicit s:Session) =
    	basicShapeJoin.filter(_._1 === id).firstOption.map(extractBasicShape).map(_.description)
}
