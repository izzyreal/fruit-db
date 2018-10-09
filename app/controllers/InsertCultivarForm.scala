package controllers

object InsertCultivarForm {
  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(name: String, speciesId: Int)

  val form = Form(
    mapping(
      "name" -> nonEmptyText, "species_id" -> number
    )(Data.apply)(Data.unapply)
  )
}
