package controllers

import play.api._
import play.api.Play
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.Play.current
import play.api.libs.json.Json
import play.api.libs.json._
import models._
import dal._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import java.io.File
import java.security.MessageDigest
import javax.inject._

import services.Authenticated

class AdminUser @Inject() (repo: UserRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index = Action.async {
    val lista = repo.list()
    lista.map( i =>
      Ok(views.html.admin.user.list(i))
    )
  }

  def addView = Authenticated {
    Ok(views.html.admin.user.add(UserForm.form))
  }

  def save() = Authenticated.async { implicit request =>
        
    UserForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.user.add(errorForm))
      },
      user => {
        repo.insert(new User(None, user.username, User.crypt(user.password), user.email))
        .map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminUser.index)
        }
        
      }
    )
  }

  def update() = Authenticated.async { implicit request =>
    UserForm.updateform.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        val id = UserForm.updateform("id").value.get
        Ok(views.html.admin.user.update(errorForm))
      },
      user => {
        val pass = repo.get(user.id).map { k => k.get.password }.toString
        val userobj = new User(Some(user.id), user.username, pass, user.email)

        repo.edit(user.id, userobj).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminUser.index)
        }
      }
    )
  }

  def editView(id:Long) = Authenticated.async {
    
    repo.get(id).map { user =>
      // TODO better 404
      if (user.isEmpty) {
        NotFound("Utilizador não encontrado.")
      } else {
        val data = Map(
          "id" -> user.get.id.get.toString,
          "username" -> user.get.username,
          "email" -> user.get.email
          )
        val id = user.get.id.get.toLong
        Ok(views.html.admin.user.update(UserForm.updateform.bind(data)))
      }
    }
  }

  /**
    * View for the login front page
    *
    * @return
    */
  def loginView() = Action {
    Ok(views.html.admin.user.login())
  }

  /**
    * authenticate method to avaliate user submited login and password
    *
    * @return
    */
  def loginAuth() = Action.async { implicit request =>
    val email :String = request.body.asFormUrlEncoded.get("username")(0).mkString
    val pass :String = request.body.asFormUrlEncoded.get("password")(0).mkString

    repo.checkAuth(email, User.crypt(pass)) map {
      user =>
        if (user.isEmpty) {
          //Unauthorized("Não autorizado. Thy shall not pass.")
          Redirect("/admin/user/login")
        } else {
          Redirect("/admin/carro").withSession("user" -> "admin")
        }
    }
  }

  def logout() = Authenticated {
    Redirect("/").withNewSession
  }

}
