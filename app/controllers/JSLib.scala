package controllers

import play.api._
import play.api.mvc._


object JSLib extends Controller {
    def jsRoutes = Action {
      implicit request =>
      import routes.javascript._
      Ok(
          Routes.javascriptRouter("jsRouter")(
              TemporaryFileManager.upload,
              TemporaryFileManager.download,
              Semiproducts.listSemiproducts,
              Semiproducts.list,
              PartDefinitions.list,
              PartDefinitions.partDefDescription,
              PartDefinitions.listFinishedParts,
              PartDefinitions.hidePartDefinition,
              CuttingPlans.cutPlanDescription,
              CuttingPlans.hideCuttingPlan,
              Semiproducts.getSemiproductDescription,
              Semiproducts.basicShapeDescription,
              Semiproducts.materialDescription,
              OrderController.list,
              OrderController.listDefinitions,
              OrderController.orderDefDescription
          )
      ).as("text/javascript")
    }

}
