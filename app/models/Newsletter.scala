package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by andref on 19-05-2016.
  */
case class Newsletter(id:Option[Long], email:String, state:String, address:String)

case class NewsletterFormData(email:String)

case class NewsletterUpdateForm(id:Option[Long], email:String, state:String, address:String)

object NewsletterForm {

  val form = Form(
    mapping(
      "email" -> nonEmptyText
    )(NewsletterFormData.apply)(NewsletterFormData.unapply)
  )

  val updateform = Form(
    mapping(
      "id" -> optional(longNumber),
      "email" -> nonEmptyText,
      "state" -> nonEmptyText,
      "address" -> nonEmptyText
    )(NewsletterUpdateForm.apply)(NewsletterUpdateForm.unapply)
  )
}