package models.semiproduct

import scala.util.parsing.combinator.RegexParsers
import models.DBAccess
import scalaz.std.option._

trait SemiproductFilter extends RegexParsers {
    self:Semiproducts with ShapeFilter with DBAccess =>

    import profile.simple._
    
    val str:Parser[String] = """\w([\w ]*\w)?""".r

    type SemiproductQueryParser = Parser[PackQueryType => PackQueryType]

    val serial:SemiproductQueryParser = "serial" ~> str ^^ { serial => query =>
        for{
            pq@(shp, pack, mat) <- query
            if Query(Semiproduct).filter(_.packId === pack.id).filter(_.serialNo === serial).exists
        } yield pq
    }

    val heat:SemiproductQueryParser = "heat" ~> str ^^ (heat => _.filter(_._2.heatNo === heat))

    val hasfree:SemiproductQueryParser = "hasfree" ^^ { x => query =>
        for {
            pq@(shp, pck, mat) <- query
            if (for {
                    sp <- Semiproduct if sp.packId === pck.id
                    c <- Cutting if c.semiproductId === sp.id && c.finishTime.isNull
                } yield sp.id).exists
        } yield pq
    }

    val semiproductFilter = heat
}
