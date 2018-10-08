package controllers

import javax.inject.Inject
import play.api.data._
import play.api.mvc._
import services.DatabaseService

class DeleteSpeciesController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import DeleteSpeciesForm._

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.DeleteSpeciesController.submitSpecies()

  def showFields = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.delete_species(form, postUrl))
  }

  // This will be the action that handles our form post
  def submitSpecies = Action { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      println("Form has errors")
      BadRequest(views.html.delete_species(formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      println("Form has no errors")

      val hasId = !data.id.isEmpty
      val hasName = !data.commonName.isEmpty

      if (hasId)
        DatabaseService.deleteSpecies(data.id.get)
      else if (hasName)
        DatabaseService.deleteSpecies(data.commonName.get)

      Redirect(routes.DeleteSpeciesController.submitSpecies())
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)

  }
}
