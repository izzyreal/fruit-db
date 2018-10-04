package controllers

import javax.inject.Inject
import models.Cultivar
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import services.DatabaseService

import scala.collection.mutable.ArrayBuffer

class CultivarsOfSpeciesController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import SpeciesForm._

  private val cultivars = ArrayBuffer[Cultivar]()

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.CultivarsOfSpeciesController.submitSpecies()

  def listCultivars = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.cultivars_of_species(cultivars, form, postUrl))
  }

  // This will be the action that handles our form post
  def submitSpecies = Action { implicit request: MessagesRequest[AnyContent] =>
    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.cultivars_of_species(cultivars, formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      // This is the good case, where the form was successfully parsed as a Data object.
      cultivars.clear()
      cultivars.appendAll(DatabaseService.listCultivarsOfSpeciesCommonName(data.name))
      Redirect(routes.CultivarsOfSpeciesController.listCultivars()).flashing("info" -> data.name)
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)
  }
}
