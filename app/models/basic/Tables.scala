package models.basic

import models._
import java.sql.Date
import java.sql.Blob
import models.enums.OrderStatusType
import models.mappers._

trait Tables { this:WithProfile =>
    import profile.simple._
    
    object Order extends Table[(Int, String, Date, Option[Date], OrderStatusType)]("order") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def name = column[String]("name")
        def fillingDate = column[Date]("filling_date")
        def dueDate = column[Option[Date]]("due_date")
        def status = column[OrderStatusType]("status")
        def * = id ~ name ~ fillingDate ~ dueDate ~ status
    }
    
    object Part extends Table[(Int, Option[Int], Int, Int, Boolean)]("part") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def orderId = column[Option[Int]]("order_id")
    	def partDefId = column[Int]("part_def_id")
    	def cutPlanId = column[Int]("cut_plan_id")
    	def damaged = column[Boolean]("damaged")
    	def * = id ~ orderId ~ partDefId ~ cutPlanId ~ damaged
    	def order = foreignKey("fk_part_order", orderId, Order)(_.id)
    	def partDefinition = foreignKey("fk_part_part_def", partDefId, PartDefinition)(_.id)
    	def cuttingPlan = foreignKey("fk_part_cut_plan", cutPlanId, CuttingPlan)(_.id)
    }
    
    object PartDefinition extends Table[(Int, Array[Byte], String, String, Boolean)]("part_definition") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def file = column[Array[Byte]]("file")
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
    	def partDefinition = foreignKey("fk_part_def_in_order_part_def", partDefId, PartDefinition)(_.id)
    }
    
    object CuttingPlan extends Table[(Int, String, Array[Byte], Boolean, String)]("cutting_plan") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def name = column[String]("name")
    	def file = column[Array[Byte]]("file")
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
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def name = column[String]("name")
    	def * = id ~ name
    }
    
    object Shape extends Table[(Int)]("shape") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def * = id
    }
    
    object Sheet extends Table[(Int, Int, Option[Double])]("sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[Double]]("thickness")
    	def * = id ~ shapeId ~ thickness
    	def shape = foreignKey("fk_sheet_shape", shapeId, Sheet)(_.id)
    }
    
    object CirclePipe extends Table[(Int, Int, Option[Double], Option[Double])]("circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[Double]]("thickness")
    	def radius = column[Option[Double]]("radius")
    	def * = id ~ shapeId ~ thickness ~ radius
    	def shape = foreignKey("fk_circle_pipe_shape", shapeId, Sheet)(_.id)
    }
    
    object SquarePipe extends Table[(Int, Int, Option[Double], Option[Double])]("square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[Double]]("thickness")
    	def diameter = column[Option[Double]]("diameter")
    	def * = id ~ shapeId ~ thickness ~ diameter
    	def shape = foreignKey("fk_square_pipe_shape", shapeId, Sheet)(_.id)
    }
    
    object ExtendedSheet extends Table[(Int, Int, Option[Double], Option[Double])]("extended_sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def sheetId = column[Int]("sheet_id")
    	def width = column[Option[Double]]("width")
    	def height = column[Option[Double]]("height")
    	def * = id ~ sheetId ~ width ~ height
    	def sheet = foreignKey("fk_extended_sheet_sheet", sheetId, Sheet)(_.id)
    }
    
    object ExtendedCirclePipe extends Table[(Int, Int, Option[Double])]("extended_circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def circlePipeId = column[Int]("circle_pipe_id")
    	def length = column[Option[Double]]("length")
    	def * = id ~ circlePipeId ~ length
    	def sheet = foreignKey("fk_extended_circle_pipe_circle_pipe", circlePipeId, Sheet)(_.id)
    }
    
    object ExtendedSquarePipe extends Table[(Int, Int, Option[Double])]("extended_square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def squarePipeId = column[Int]("square_pipe_id")
    	def length = column[Option[Double]]("length")
    	def * = id ~ squarePipeId ~ length
    	def sheet = foreignKey("fk_extended_square_pipe_square_pipe", squarePipeId, Sheet)(_.id)
    }
    
    object Pack extends Table[(Int, Int, Int, Boolean, Date, String)]("pack") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def materialId = column[Int]("material_id")
    	def unlimited = column[Boolean]("unlimited")
    	def shapeId = column[Int]("shape_id")
    	def deliveryDate = column[Date]("delivery_date")
    	def heatNo = column[String]("heat_no")
    	def * = id ~ materialId ~ shapeId ~ unlimited ~ deliveryDate ~ heatNo
    	def shape = foreignKey("fk_pack_shape", shapeId, Shape)(_.id)
    	def material = foreignKey("fk_pack_material", materialId, Material)(_.id)
    }
    
    object Semiproduct extends Table[(Int, Int, String)]("semiproduct") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def packId = column[Int]("id")
    	def serialNo = column[String]("serial_no")
    	def * = id ~ packId ~ serialNo
    	def pack = foreignKey("fk_semiproduct_pack", packId, Pack)(_.id)
    }
    
    object Cutting extends Table[(Int, Option[Date], Int)]("cutting") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def finishTime = column[Option[Date]]("finish_time")
    	def semiproductId = column[Int]("semiproduct_id")
    	def * = id ~ finishTime ~ semiproductId
    }
}

