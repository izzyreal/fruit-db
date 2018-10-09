package controllers

object InsertSpeciesForm {
  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(genus: String, species: String, commonName: String)

  val form = Form(
    mapping(
      "genus" -> nonEmptyText, "species" -> nonEmptyText, "common_name" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )
}
