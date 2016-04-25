
package models

import play.api.data.Form
import play.api.data.Forms._

case class User(id: Option[Long], username: String, password: String, email:String)

case class UserFormData(id:String, username: String, password: String, email:String)

object UserForm {

  val form = Form(
    mapping(
      "id" -> text,
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText
    )(UserFormData.apply)(UserFormData.unapply)
  )
}