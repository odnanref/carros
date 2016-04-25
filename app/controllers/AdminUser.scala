package controllers

import play.api._
import play.api.Play
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.Play.current // for getting the current application path
import play.api.libs.json.Json
import play.api.libs.json._

import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{Success, Failure}

import java.io.File

import javax.inject._

class AdminUser @Inject() (repo: UserRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index = Action.async {
    val lista = repo.list()
    lista.map( i =>
      Ok(views.html.admin.user.list(i))
    )
  }

  def addView = Action {
    Ok(views.html.admin.user.add(UserForm.form))
  }

  def save() = Action.async { implicit request =>
        
    UserForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.user.add(errorForm))
      },
      user => {
        repo.insert(new User(None, user.username, user.password, user.email))
        .map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminUser.index)
        }
        
      }
    )
  }

  def update() = Action.async { implicit request =>

    UserForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        val id = UserForm.form("id").value.get.toLong
        Ok(views.html.admin.user.update(errorForm))
      },
      user => {        
        val userobj = new User( Some(user.id.toLong), user.username, user.password, 
          user.email)

        repo.edit(user.id.toLong, userobj).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminUser.index)
        }
      }
    )
  }

  def editView(id:Long) = Action.async {
    
    repo.get(id).map { user =>
      // TODO better 404
      if (user == None) {
        NotFound
      } else {

      val data = Map(
        "id" -> user.get.id.get.toString, 
        "username" -> user.get.username,
        "password" -> user.get.password,
        "email" -> user.get.email
        )
      val id = user.get.id.get.toLong
      Ok(views.html.admin.user.update(UserForm.form.bind(data)))
      }
      // TODO make this show a not found personalised page
    }
  }
}
