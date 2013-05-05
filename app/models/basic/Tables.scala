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
    	def orderDefId = column[Option[Int]]("order_def_id")
    	def partDefId = column[Int]("part_def_id")
    	def cuttingId = column[Int]("cutting_id")
    	def damaged = column[Boolean]("damaged")
    	def * = id ~ orderDefId ~ partDefId ~ cuttingId ~ damaged
    	def orderDef = foreignKey("fk_part_order_def", orderDefId, OrderDefinition)(_.id)
    	def partDefinition = foreignKey("fk_part_part_def", partDefId, PartDefinition)(_.id)
    	def cutting = foreignKey("fk_part_cutting", cuttingId, Cutting)(_.id)
    	def forInsert = orderDefId ~ partDefId ~ cuttingId ~ damaged
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
    
    object OrderDefinition extends Table[(Int, Int, Int, Int, String)]("order_def") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def orderId = column[Int]("order_id")
    	def partDefId = column[Int]("part_def_id")
    	def count = column[Int]("count")
    	def filter = column[String]("filter")
    	def * = id ~ orderId ~ partDefId ~ count ~ filter
    	def order = foreignKey("fk_order_def_order", orderId, Order)(_.id)
    	def partDefinition = foreignKey("fk_order_def_part_def", partDefId, PartDefinition)(_.id)
    	def forInsert = orderId ~ partDefId ~ count ~ filter
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
    
    object Shape extends Table[(Int, Int, Option[Int])]("shape") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def basicShapeId = column[Int]("basic_shape_id")
    	def extendedShapeId = column[Option[Int]]("extended_shape_id")
    	def * = id ~ basicShapeId ~ extendedShapeId
    	def basicShape = foreignKey("fk_shape_basic_shape", basicShapeId, CommonShape)(_.id)
    	def extendedShape = foreignKey("fk_shape_extended_shape", extendedShapeId, CommonShape)(_.id)
    	def forInsert = id returning id
    }
    
    object CommonShape extends Table[(Int)]("common_shape") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def * = id
    	def forInsert = id returning id
    }
    
    object Sheet extends Table[(Int, Int, Option[BigDecimal])]("sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def commonShapeId = column[Int]("common_shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def * = id ~ commonShapeId ~ thickness
    	def shape = foreignKey("fk_sheet_common_shape", commonShapeId, CommonShape)(_.id)
    	def forInsert = commonShapeId ~ thickness returning id
    }
    
    object CirclePipe extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("circle_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def commonShapeId = column[Int]("common_shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def radius = column[Option[BigDecimal]]("radius")
    	def * = id ~ commonShapeId ~ thickness ~ radius
    	def shape = foreignKey("fk_circle_pipe_common_shape", commonShapeId, CommonShape)(_.id)
    	def forInsert = commonShapeId ~ thickness ~ radius returning id
    }
    
    object SquarePipe extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("square_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def commonShapeId = column[Int]("common_shape_id")
    	def thickness = column[Option[BigDecimal]]("thickness")
    	def diameter = column[Option[BigDecimal]]("diameter")
    	def * = id ~ commonShapeId ~ thickness ~ diameter
    	def shape = foreignKey("fk_square_pipe_common_shape", commonShapeId, CommonShape)(_.id)
    	def forInsert = commonShapeId ~ thickness ~ diameter returning id
    }
    
    object ExtendedSheet extends Table[(Int, Int, Option[BigDecimal], Option[BigDecimal])]("extended_sheet") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def commonShapeId = column[Int]("common_shape_id")
    	def width = column[Option[BigDecimal]]("width")
    	def height = column[Option[BigDecimal]]("height")
    	def * = id ~ commonShapeId ~ width ~ height
    	def shape = foreignKey("fk_extended_common_shape", commonShapeId, CommonShape)(_.id)
    	def forInsert = commonShapeId ~ width ~ height returning id
    }
    
    object ExtendedPipe extends Table[(Int, Int, Option[BigDecimal])]("extended_pipe") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def commonShapeId = column[Int]("common_shape_id")
    	def length = column[Option[BigDecimal]]("length")
    	def * = id ~ commonShapeId ~ length
    	def shape = foreignKey("fk_extended_pipe_common_shape", commonShapeId, CommonShape)(_.id)
    	def forInsert = commonShapeId ~ length returning id
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
    	def packId = column[Int]("pack_id")
    	def serialNo = column[String]("serial_no")
    	def * = id ~ packId ~ serialNo
    	def pack = foreignKey("fk_semiproduct_pack", packId, Pack)(_.id)
    	def forInsert = packId ~ serialNo returning id
    }
    
    object Cutting extends Table[(Int, Option[DateTime], Int)]("cutting") {
    	def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    	def finishTime = column[Option[DateTime]]("finish_time")
    	def semiproductId = column[Int]("semiproduct_id")
        def cuttingPlanId = column[Int]("cutplan_id")
    	def * = id ~ finishTime ~ semiproductId
        def semiproduct = foreignKey("fk_cutting_semiproduct", semiproductId, Semiproduct)(_.id)
        def cuttingPlan = foreignKey("fk_cutting_cutting_plan", cuttingPlanId, CuttingPlan)(_.id)
        def forInsert = finishTime ~ semiproductId ~ cuttingPlanId returning id
    }
}

