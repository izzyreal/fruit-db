package controllers

import javax.inject.Inject
import play.api.data._
import play.api.mvc._
import services.DatabaseService

class DeleteCultivarController @Inject()(cc: MessagesControllerComponents) extends MessagesAbstractController(cc) {
  import DeleteCultivarForm._

  // The URL to the widget.  You can call this directly from the template, but it
  // can be more convenient to leave the template completely stateless i.e. all
  // of the "WidgetController" references are inside the .scala file.
  private val postUrl = routes.DeleteCultivarController.submitCultivar()

  def showFields = Action { implicit request: MessagesRequest[AnyContent] =>
    // Pass an unpopulated form to the template
    Ok(views.html.delete_cultivar(form, postUrl))
  }

  // This will be the action that handles our form post
  def submitCultivar = Action { implicit request: MessagesRequest[AnyContent] =>

    val errorFunction = { formWithErrors: Form[Data] =>
      // This is the bad case, where the form had validation errors.
      // Let's show the user the form again, with the errors highlighted.
      // Note how we pass the form with errors to the template.
      println("Form has errors")
      BadRequest(views.html.delete_cultivar(formWithErrors, postUrl))
    }

    val successFunction = { data: Data =>
      println("Form has no errors")

      val hasId = !data.id.isEmpty
      val hasName = !data.name.isEmpty

      if (hasId)
        DatabaseService.deleteCultivar(data.id.get)
      else if (hasName)
        DatabaseService.deleteCultivar(data.name.get)

      Redirect(routes.DeleteCultivarController.submitCultivar())
    }

    val formValidationResult = form.bindFromRequest
    formValidationResult.fold(errorFunction, successFunction)

  }
}
