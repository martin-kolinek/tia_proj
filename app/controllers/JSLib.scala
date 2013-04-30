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
              Semiproducts.listSemiproducts
          )
      ).as("text/javascript")
    }

}
