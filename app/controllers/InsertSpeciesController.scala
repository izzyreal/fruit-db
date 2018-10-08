package controllers

import javax.inject.Inject
import play.api.data._
import play.api.mvc._
import services.DatabaseService

class InsertNewSpeciesController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import InsertNewSpeciesForm._

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.InsertNewSpeciesController.submitSpecies()

  def showFields = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.insert_new_species(form, postUrl))
  }

  // This will be the action that handles our form post
  def submitSpecies = Action { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      BadRequest(views.html.insert_new_species(formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      DatabaseService.insertNewSpecies(data.genus, data.species, data.commonName)
      Redirect(routes.InsertNewSpeciesController.submitSpecies()).flashing("info" -> data.commonName)
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)

  }
}
