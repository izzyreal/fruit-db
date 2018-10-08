package controllers

object DeleteSpeciesForm {
  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(id: Option[Int], commonName: Option[String])

  val form = Form(
    mapping(
      "species_id" -> optional(number), "species_common_name" -> optional(text))(Data.apply)(Data.unapply)
  )
}
