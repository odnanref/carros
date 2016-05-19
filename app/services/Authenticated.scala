
package services

import play.api.mvc._
import play.api.mvc.Results._

import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

object Authenticated extends ActionBuilder[Request] {
  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) = {
  	val user = request.session.get("user")
    if(user.isDefined)
      block(request)
    else
      Future { Redirect("/admin/user/login") }
  }
}