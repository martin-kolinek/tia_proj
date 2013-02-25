package models.basic

import models._
import java.sql.Blob
import models.enums.OrderStatusType
import models.mappers._
import org.joda.time.DateTime

trait Tables { this:DBAccess =>
    import profile.simple._
    
    object Order extends Table[(Int, String, DateTime, Option[DateTime], OrderStatusType)]("order") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def name = column[String]("name")
        def fillingDate = column[DateTime]("filling_date")
        def dueDate = column[Option[DateTime]]("due_date")
        def status = column[OrderStatusType]("status")
        def * = id ~ name ~ fillingDate ~ dueDate ~ status
        def forInsert = name ~ fillingDate ~ dueDate ~ status returning id
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
    	def forInsert = file ~ filter ~ name ~ hidden returning id
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
    	def forInsert = name ~ filter ~ file ~ hidden returning id
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
    
    case class DBMaterial(id:Int, name:String) {}
    
    object Material extends Table[DBMaterial]("material") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def name = column[String]("name")
    	def * = id ~ name <> (DBMaterial, DBMaterial.unapply _)
    	def forInsert = name returning id
    }
    
    object Shape extends Table[(Int)]("shape") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def * = id
    	def forInsert = id returning id
    }
    
    object Sheet extends Table[(Int, Int, Option[BigDecimal])]("sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def * = id ~ shapeId ~ thickness
    	def shape = foreignKey("fk_sheet_shape", shapeId, Sheet)(_.id)
    	def forInsert = shapeId ~ thickness returning id
    }
    
    object CirclePipe extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def radius = column[Option[BigDecimal]]("radius")
    	def * = id ~ shapeId ~ thickness ~ radius
    	def shape = foreignKey("fk_circle_pipe_shape", shapeId, Shape)(_.id)
    	def forInsert = shapeId ~ thickness ~ radius returning id
    }
    
    object SquarePipe extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def shapeId = column[Int]("shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def diameter = column[Option[BigDecimal]]("diameter")
    	def * = id ~ shapeId ~ thickness ~ diameter
    	def shape = foreignKey("fk_square_pipe_shape", shapeId, Shape)(_.id)
    	def forInsert = shapeId ~ thickness ~ diameter returning id
    }
    
    object ExtendedSheet extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("extended_sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def sheetId = column[Int]("sheet_id")
    	def width = column[Option[BigDecimal]]("width")
    	def height = column[Option[BigDecimal]]("height")
    	def * = id ~ sheetId ~ width ~ height
    	def sheet = foreignKey("fk_extended_sheet_sheet", sheetId, Sheet)(_.id)
    	def forInsert = sheetId ~ width ~ height returning id
    }
    
    object ExtendedCirclePipe extends Table[(Int, Int, Option[BigDecimal])]("extended_circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def circlePipeId = column[Int]("circle_pipe_id")
    	def length = column[Option[BigDecimal]]("length")
    	def * = id ~ circlePipeId ~ length
    	def circlePipe = foreignKey("fk_extended_circle_pipe_circle_pipe", circlePipeId, CirclePipe)(_.id)
    	def forInsert = circlePipeId ~ length returning id
    }
    
    object ExtendedSquarePipe extends Table[(Int, Int, Option[BigDecimal])]("extended_square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def squarePipeId = column[Int]("square_pipe_id")
    	def length = column[Option[BigDecimal]]("length")
    	def * = id ~ squarePipeId ~ length
    	def squarePipe = foreignKey("fk_extended_square_pipe_square_pipe", squarePipeId, SquarePipe)(_.id)
    	def forInsert = squarePipeId ~ length returning id
    }
    
    case class DBPack(id:Int, materialId:Int, shapeId:Int, unlimited:Boolean, deliveryDate:DateTime, heatNo:String) {}
    
    object Pack extends Table[DBPack]("pack") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def materialId = column[Int]("material_id")
    	def unlimited = column[Boolean]("unlimited")
    	def shapeId = column[Int]("shape_id")
    	def deliveryDate = column[DateTime]("delivery_date")
    	def heatNo = column[String]("heat_no")
    	def * = id ~ materialId ~ shapeId ~ unlimited ~ deliveryDate ~ heatNo <> (DBPack, DBPack.unapply _)
    	def shape = foreignKey("fk_pack_shape", shapeId, Shape)(_.id)
    	def material = foreignKey("fk_pack_material", materialId, Material)(_.id)
    	def forInsert = materialId ~ unlimited ~ shapeId ~ deliveryDate ~ heatNo returning id
    }
    
    object Semiproduct extends Table[(Int, Int, String)]("semiproduct") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def packId = column[Int]("id")
    	def serialNo = column[String]("serial_no")
    	def * = id ~ packId ~ serialNo
    	def pack = foreignKey("fk_semiproduct_pack", packId, Pack)(_.id)
    	def forInsert = packId ~ serialNo returning id
    }
    
    object Cutting extends Table[(Int, Option[DateTime], Int)]("cutting") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def finishTime = column[Option[DateTime]]("finish_time")
    	def semiproductId = column[Int]("semiproduct_id")
    	def * = id ~ finishTime ~ semiproductId
    }
}

