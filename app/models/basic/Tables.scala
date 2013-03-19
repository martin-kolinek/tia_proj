package model.basic

import scala.slick.driver.ExtendedProfile
import models._
import java.sql.Date
import java.sql.Blob
import models.enums.OrderStatusType
import models.enums.PartStatusType
import models.mappers._

trait WithProfile {
    val profile:ExtendedProfile
}

trait Tables { this:WithProfile =>
    import profile.simple._
    
    object Order extends Table[(Int, String, Date, Option[Date], OrderStatusType)]("order") {
        def id = column[Int]("id", O.PrimaryKey)
        def name = column[String]("name")
        def fillingDate = column[Date]("filling_date")
        def dueDate = column[Option[Date]]("due_date")
        def status = column[OrderStatusType]("status")
        def * = id ~ name ~ fillingDate ~ dueDate ~ status
    }
    
    object Part extends Table[(Int, Option[Int], Int, PartStatusType)]("part") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def orderId = column[Option[Int]]("order_id")
    	def partDefId = column[Int]("part_def_id")
    	def status = column[PartStatusType]("status")
    	def * = id ~ orderId ~ partDefId ~ status
    	def order = foreignKey("fk_part_order", orderId, Order)(_.id)
    	def partDefinition = foreignKey("fk_part_part_def", partDefId, PartDefinition)(_.id)
    }
    
    object PartDefinition extends Table[(Int, Blob, String, String, Boolean)]("part_definition") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def file = column[Blob]("file")
    	def filter = column[String]("filter")
    	def name = column[String]("name")
    	def hidden = column[Boolean]("hidden")
    	def * = id ~ file ~ filter ~ name ~ hidden
    }
    
    object PartDefinitionInOrder extends Table[(Int, Int, Int, String)]("part_def_in_order") {
    	def orderId = column[Int]("order_id")
    	def partDefId = column[Int]("part_def_id")
    	def count = column[Int]("count")
    	def filter = column[String]("filter")
    	def * = orderId ~ partDefId ~ count ~ filter
    	def pk = primaryKey("pk_part_def_in_order", (orderId, partDefId))
    	def order = foreignKey("fk_part_def_in_order_order", orderId, Order)(_.id)
    	def partDefinition = foreignKey("fk_part_def_in_order_", partDefId, PartDefinition)(_.id)
    }
    
    object CuttingPlan extends Table[(Int, String, Blob, Boolean, String)]("cutting_plan") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def name = column[String]("name")
    	def file = column[Blob]("file")
    	def hidden = column[Boolean]("hidden")
    	def filter = column[String]("filter")
    	def * = id ~ name ~ file ~ hidden ~ filter
    }
    
    object PartDefinitionInCuttingPlan extends Table[(Int, Int, Int)]("part_def_in_cut_plan") {
    	def cutPlanId = column[Int]("cut_plan_id")
    	def partDefId = column[Int]("part_def_id")
    	def count = column[Int]("count")
    	def * = cutPlanId ~ partDefId ~ count
    	def pk = primaryKey("pk_part_def_in_cut_plan", (cutPlanId, partDefId))
    	def cuttingPlan = foreignKey("fk_part_def_in_cut_plan_cut_plan", cutPlanId, CuttingPlan)(_.id)
    	def partDefinition = foreignKey("fk_part_def_in_cut_plan_part_def", partDefId, PartDefinition)(_.id)
    }
    
    object Material extends Table[(Int, String)]("material") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def name = column[String]("name")
    	def * = id ~ name
    }
    
    object Shape extends Table[(Int, String)]("shape") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def textDescription = column[String]("text_description")
    	def * = id ~ textDescription
    }
    
    object Sheet extends Table[(Int, Int, Double)]("sheet") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Double]("thickness")
    	def * = id ~ shapeId ~ thickness
    	def shape = foreignKey("fk_sheet_shape", shapeId, Sheet)(_.id)
    }
    
    object CirclePipe extends Table[(Int, Int, Double, Double)]("circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Double]("thickness")
    	def radius = column[Double]("radius")
    	def * = id ~ shapeId ~ thickness ~ radius
    	def shape = foreignKey("fk_circle_pipe_shape", shapeId, Sheet)(_.id)
    }
    
    object SquarePipe extends Table[(Int, Int, Double, Double)]("square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Double]("thickness")
    	def diameter = column[Double]("diameter")
    	def * = id ~ shapeId ~ thickness ~ diameter
    	def shape = foreignKey("fk_circle_pipe_shape", shapeId, Sheet)(_.id)
    }
    
    object ExtendedSheet extends Table[(Int, Int, Double, Double)]("extended_sheet") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def sheetId = column[Int]("sheet_id")
    	def width = column[Double]("width")
    	def height = column[Double]("height")
    	def * = id ~ sheetId ~ width ~ height
    	def sheet = foreignKey("fk_extended_sheet_sheet", sheetId, Sheet)(_.id)
    }
    
    object ExtendedCirclePipe extends Table[(Int, Int, Double)]("extended_circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def circlePipeId = column[Int]("circle_pipe_id")
    	def length = column[Double]("length")
    	def * = id ~ circlePipeId ~ length
    	def sheet = foreignKey("fk_extended_circle_pipe_circle_pipe", circlePipeId, Sheet)(_.id)
    }
    
    object ExtendedSquarePipe extends Table[(Int, Int, Double)]("extended_square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey)
    	def squarePipeId = column[Int]("square_pipe_id")
    	def length = column[Double]("length")
    	def * = id ~ squarePipeId ~ length
    	def sheet = foreignKey("fk_extended_square_pipe_square_pipe", squarePipeId, Sheet)(_.id)
    }
    
    //ject Cutting extends Table[(Int, Date, )]
}
