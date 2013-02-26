package controllers

import play.api._
import play.api.mvc._

object JSRoutes extends Controller {
    def tempFileRoutes = Action {
      implicit request =>
      import routes.javascript._
      Ok(
          Routes.javascriptRouter("tempFileRoutes")(
              TemporaryFileManager.upload,
              TemporaryFileManager.download
          )
      ).as("text/javascript")
    }
}
