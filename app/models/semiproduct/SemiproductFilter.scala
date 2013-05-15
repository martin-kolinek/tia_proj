package models.semiproduct

import scala.util.parsing.combinator.RegexParsers
import models.DBAccess
import scalaz.std.option._
import models.ValidationParser

trait SemiproductFilter extends RegexParsers with ValidationParser {
    self:Semiproducts with ShapeFilter with DBAccess =>

    import profile.simple._
    
    val str:Parser[String] = """\w([\w ]*\w)?""".r

    type PackFilter = PackFilterInput => Column[Option[Boolean]]
    
    type SemiproductQueryParser = Parser[PackFilter]

    val serial:SemiproductQueryParser = "serial" ~> str ^^ { serial => pck =>
        Query(Semiproduct).filter(_.packId === pck._2.id).filter(_.serialNo === serial).exists
    }

    val heat:SemiproductQueryParser = "heat" ~> str ^^ (heat => _._2.heatNo === heat)

    val mat:SemiproductQueryParser = "material" ~> str ^^ (mat => _._3.name === mat)

    val hasfree:SemiproductQueryParser = "hasfree" ^^ { x => pck =>
        (for {
        	sp <- Semiproduct if sp.packId === pck._2.id
        	if !Query(Cutting).filter(_.semiproductId === sp.id).exists
        } yield sp.id).exists
    }
    
    val packShapeParsers = shapeParsers.map { pars =>
    	pars ^^ {filt => pck:PackFilterInput => filt(pck._1)}
    }
    
    val none:SemiproductQueryParser = "" ^^ {x => y => true.?}
    
    val packParsers = List(serial, heat, hasfree, mat) ++ packShapeParsers
    
    val packElement = (packParsers.head /: packParsers.tail)(_|_) | none
    
    val packFilterParser = repsep(packElement, ",")
    
    def parseSemiproductFitler(str:String) = toValidation(parseAll(packFilterParser, str))
}
