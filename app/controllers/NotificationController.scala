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

class NotificationController @Inject() (repo: NotificationRepository, repoMarca: MarcaRepository,
                                        repoModelo:ModeloRepository, val messagesApi: MessagesApi)
                                     (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index(page: Int) = Authenticated.async {
    val lista = repo.list(page)
    lista.flatMap( i =>
      repo.count().map { total =>
        Ok(views.html.admin.notification.list(i, total/repo.PAGESIZE))
      }
    )
  }

  def addView = Authenticated {
    Ok(views.html.admin.notification.add())
  }

  def publicAdd = Action.async {
    // will be directly iterated for select combo box
    repoMarca.list().flatMap {
      brands =>
    // should be iterated to json in the html code
      repoModelo.list().map { models =>
        Ok(views.html.notification(brands, models))
      }
    }
  }

  def save() = Authenticated.async { implicit request =>

    NotificationForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.notification.add())
      },
      news => {
        repo.insert(Notification(None, news.email, news.make, news.model))
          .map { _ =>
            // If successful, we simply redirect to the index page.
            Ok(views.html.notification_success())
          }

      }
    )
  }

  def update() = Authenticated.async { implicit request =>
    NotificationForm.updateform.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.notification.update(errorForm))
      },
      news => {
        val newsobj = new Notification(news.id, news.email, news.model, news.make)

        repo.edit(news.id.get, newsobj).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.NotificationController.editView(news.id.getOrElse(0)))
        }
      }
    )
  }

  def editView(id:Long) = Authenticated.async {

    repo.get(id).map { news =>
      // TODO better 404
      if (news.isEmpty) {
        NotFound("Email não encontrado.")
      } else {
        val data = Map(
          "id" -> news.get.id.get.toString,
          "email" -> news.get.email,
          "make" -> news.get.make.toString,
          "model" -> news.get.model.toString
        )
        val id = news.get.id.get
        Ok(views.html.admin.notification.update(NotificationForm.updateform.bind(data))) // FIXME this is not ok
      }
    }
  }

  def remove(id:Long) = TODO

  implicit val responseFormat = Json.format[JsonResponse]
  implicit val ReturnModelFormat = Json.format[Modelo]
  implicit val ReturnMarcaFormat = Json.format[Marca]

  def getModelByBrand(id:Long) = Action.async {
    repoModelo.getMarcaByModelo(id).map {
      lista =>
        if (lista != None ) {
          Ok(Json.toJson(lista))
        } else {
          val ret = new JsonResponse("erro", "Sem resultados")
          Ok(Json.toJson(ret))
        }
    }
  }

  def getBrands() = Action.async {
    repoMarca.list().map {
      lista =>
      val ret = Json.toJson(lista)
        Ok(ret)
    }
  }

}
