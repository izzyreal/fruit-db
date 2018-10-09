package controllers

object DeleteCultivarForm {
  import play.api.data.Form
  import play.api.data.Forms._

  case class Data(id: Option[Int], name: Option[String])

  val form = Form(
    mapping(
      "cultivar_id" -> optional(number), "cultivar_name" -> optional(text))(Data.apply)(Data.unapply)
  )
}
