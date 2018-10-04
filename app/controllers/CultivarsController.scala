package controllers

import javax.inject._
import play.api.mvc._
import services.DatabaseService

@Singleton
class CultivarsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def list() = Action { implicit request: Request[AnyContent] =>

    Ok(DatabaseService.listCultivars().toString)
  }

}
