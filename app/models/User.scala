
package models

import play.api.data.Form
import play.api.data.Forms._

import java.security.MessageDigest

case class User(id: Option[Long], username: String, password: String, email:String)

case class UserFormData(id:Long, username: String, password: Option[String], email:String)

object UserForm {

  val form = Form(
    mapping(
      "id" -> optional(longNumber),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> nonEmptyText
    )(User.apply)(User.unapply)
  )

  val updateform = Form(
    mapping(
      "id" -> longNumber,
      "username" -> nonEmptyText,
      "password" -> optional(text),
      "email" -> nonEmptyText
    )(UserFormData.apply)(UserFormData.unapply)
  )

}

object User {
  def crypt(p:String) :String = {
    val md :MessageDigest = MessageDigest.getInstance("SHA-256");
    md.update(p.getBytes("UTF-8")); // Change this to "UTF-16" if needed
    val digest = md.digest()
    val password = digest.mkString
    password
  }
}