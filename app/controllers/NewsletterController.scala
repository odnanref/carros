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

class NewsletterController @Inject() (repo: NewsletterRepository, val messagesApi: MessagesApi)
                          (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index = Action.async {
    val lista = repo.list()
    lista.map( i =>
      Ok(views.html.admin.newsletter.list(i))
    )
  }

  def addView = Authenticated {
    Ok(views.html.admin.newsletter.add())
  }

  def publicAdd = TODO

  def save() = Authenticated.async { implicit request =>

    NewsletterForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.newsletter.add())
      },
      news => {
        repo.insert(Newsletter(None, news.email, "active", request.remoteAddress))
          .map { _ =>
            // If successful, we simply redirect to the index page.
            Redirect(routes.NewsletterController.index)
          }

      }
    )
  }

  def update() = Authenticated.async { implicit request =>
    NewsletterForm.updateform.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.newsletter.update(errorForm))
      },
      news => {
        val newsobj = new Newsletter(news.id, news.email, news.state, news.address)

        repo.edit(news.id.get, newsobj).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.NewsletterController.index)
        }
      }
    )
  }

  def editView(id:Long) = Authenticated.async {

    repo.get(id).map { news =>
      // TODO better 404
      if (!news.isDefined) {
        NotFound("Email não encontrado.")
      } else {
        val data = Map(
          "id" -> news.get.id.get.toString,
          "email" -> news.get.email
        )
        val id = news.get.id.get
        Ok(views.html.admin.newsletter.update(NewsletterForm.updateform.bind(data))) // FIXME this is not ok
      }
    }
  }

  def remove(id:Long) = TODO

}
