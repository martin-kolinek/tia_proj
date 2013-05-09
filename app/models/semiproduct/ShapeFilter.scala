package models.semiproduct

import scala.util.parsing.combinator.RegexParsers
import models.DBAccess
import scalaz.std.option._

trait ShapeFilter extends RegexParsers {
    self:Shapes with DBAccess =>

    import profile.simple._

    val number = """\d+(\.\d*)?""".r ^^ BigDecimal.apply _

    type BigDecimalColParser = Parser[BigDecimal => Column[Option[BigDecimal]] => Column[Option[Boolean]]]

    val atLeast:BigDecimalColParser = ">" ^^ (x => n => c => c >= some(n))

    val atMost:BigDecimalColParser = "<" ^^ (x => n => c => c <= some(n))

    val exactly:BigDecimalColParser = "=" ^^ (x => n => c => c === some(n))

    def constraint(attr:String):Parser[Column[Option[BigDecimal]] => Column[Option[Boolean]]] = attr ~> (atLeast | atMost | exactly).? ~ number <~ "mm".? ^^ {
        case Some(op) ~ num => op(num)
        case None ~ num => col => col === some(num)
    }

    type ShapeQueryParser = Parser[ShapeFilterInput => Column[Option[Boolean]]]

    val sheet:ShapeQueryParser = "sheet" ^^ (str => _._2._1.isNotNull)

    val circPipe:ShapeQueryParser = "circle" ^^ (str => _._3._1.isNotNull)

    val squarePipe:ShapeQueryParser = "square" ^^ (str => _._4._1.isNotNull)

    val thickness:ShapeQueryParser = constraint("thick") ^^ (con => shp => con(shp._2._2) || con(shp._3._3) || con(shp._4._3))

    val width:ShapeQueryParser = constraint("width") ^^ (con => shp => con(shp._5._2))
  
    val height:ShapeQueryParser = constraint("height") ^^ (con => shp => con(shp._5._3))

    val radius:ShapeQueryParser = constraint("radius") ^^ (con => shp => con(shp._3._2))

    val diameter:ShapeQueryParser = constraint("diam") ^^ (con => shp => con(shp._4._2))

    val length:ShapeQueryParser = constraint("length") ^^ (con => shp => con(shp._6._2))
    
    val shapeParsers = List(sheet, circPipe, squarePipe, thickness, width, height, radius, diameter, length)
}
