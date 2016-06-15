package models

import play.api.data.Form
import play.api.data.Forms._

/**
  * Notification Alert support code
  *
  * Created by andref on 05-06-2016.
  */
case class Notification(id: Option[Long], email:String, make: Long, model: Long)

case class NotificationFormData(email:String, make: Long, model: Long)

case class NotificationUpdateForm(id:Option[Long], email:String, make: Long, model: Long)

object NotificationForm {

  val form = Form(
    mapping(
      "email" -> nonEmptyText,
      "make" -> longNumber,
      "model" -> longNumber
    )(NotificationFormData.apply)(NotificationFormData.unapply)
  )

  val updateform = Form(
    mapping(
      "id" -> optional(longNumber),
      "email" -> nonEmptyText,
      "make" -> longNumber,
      "model" -> longNumber
    )(NotificationUpdateForm.apply)(NotificationUpdateForm.unapply)
  )
}