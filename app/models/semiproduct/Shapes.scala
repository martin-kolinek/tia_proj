package models.semiproduct

import models.basic.Tables
import models.DBAccess
import models.DBAccess

case class OptionSheet(id:Option[Int], thickness:Option[Double])
case class OptionCirclePipe(id:Option[Int], radius:Option[Double], thickness:Option[Double])
case class OptionSquarePipe(id:Option[Int], diameter:Option[Double], thickness:Option[Double])
case class OptionExtendedSheet(id:Option[Int], width:Option[Double], height:Option[Double])
case class OptionExtendedPipe(id:Option[Int], length:Option[Double])

class ShapeDesc protected() {
	def description = "something" 
}

object ShapeDesc extends ShapeDesc {
}

case class SheetDesc(thickness:Option[Double], width:Option[Double], height:Option[Double]) extends ShapeDesc {
	override def description = s"sheet - $thickness mm, $width x $height"
}

case class CirclePipeDesc(thickness:Option[Double], radius:Option[Double], length:Option[Double]) extends ShapeDesc {
	override def description = s"circle pipe - $thickness, R=$radius, L=$length" 
} 

case class SquarePipeDesc(thickness:Option[Double], diameter:Option[Double], length:Option[Double]) extends ShapeDesc {
	override def description = s"square pipe - $thickness, D=$diameter, L=$length"
}

trait Shapes extends Tables { this:DBAccess =>
	import profile.simple._
	
	def optionSheet(s:Sheet.type) = s.id.? ~ s.thickness <> (OptionSheet, OptionSheet.unapply _)
		
	def optionCirclePipe(c:CirclePipe.type) = c.id.? ~ c.radius ~ c.thickness <> (OptionCirclePipe, OptionCirclePipe.unapply _)
				
	def optionSquarePipe(s:SquarePipe.type) = s.id.? ~ s.diameter ~ s.thickness <> (OptionSquarePipe, OptionSquarePipe.unapply _)
		
	def optionExtendedSheet(s:ExtendedSheet.type) = s.id.? ~ s.width ~ s.height <> (OptionExtendedSheet, OptionExtendedSheet.unapply _)
		
	def optionExtendedCirclePipe(p:ExtendedCirclePipe.type) = p.id.? ~ p.length <> (OptionExtendedPipe, OptionExtendedPipe.unapply _)
		
	def optionExtendedSquarePipe(p:ExtendedSquarePipe.type) = p.id.? ~ p.length <> (OptionExtendedPipe, OptionExtendedPipe.unapply _)
		
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
    	case (_, OptionSheet(Some(_), thick), _, _, OptionExtendedSheet(_, width, height), _, _) => SheetDesc(thick, width, height)
    	case (_, _, OptionCirclePipe(Some(_), rad, thick), _, _, OptionExtendedPipe(_, len), _) => CirclePipeDesc(thick, rad, len)
    	case (_, _, _, OptionSquarePipe(Some(_), diam, thick), _, _, OptionExtendedPipe(_, len)) => SquarePipeDesc(thick, diam, len)
    	case x => ShapeDesc
    }
    
    private val nextval = SimpleFunction.unary[String, Int]("nextval")
    
    def insertShape(implicit session:Session) = Shape.forInsert.insert(Query(nextval("shape_id_seq"))).head
}