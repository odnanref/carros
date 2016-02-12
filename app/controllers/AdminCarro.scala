package controllers

import play.api._
import play.api.Play
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.Play.current // for getting the current application path

import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{Success, Failure}

import javax.inject._

class AdminCarro @Inject() (repo: CarroRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def index = Action.async {
    val lista = repo.list()
    lista.map( i =>
    Ok(views.html.admin.list(i))
    )
  }

  def carro( id:Long) = Action {
  	
  	val car = new Carro(1, "BMW M6", "5 portas, de 2005", "bmw-m6.jpeg", "bmw, m6, 5 portas")
  	Ok(views.html.admin.carro(car))
  }

  def addView = Action {
    Ok(views.html.admin.index(CarroForm.form))
  }

  def add = Action.async(parse.multipartFormData) { implicit request =>
    
    val filename = handleUpload(request)
    println("filename " + filename)
    CarroForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.index(errorForm))
      },
      carro => 
        repo.create(carro.name, carro.description, filename.getOrElse("logo.png"), carro.keywords).map { _ =>
		      // If successful, we simply redirect to the index page.
		      Redirect(routes.Application.index)
        }
      
    )

  }

  def uploadView = Action { request =>
    Ok(views.html.admin.upload())
  }

  def upload = Action(parse.multipartFormData) { request => 
  	request.body.file("picture").map { picture =>
	    import java.io.File
	    val filename = picture.filename
	    val contentType = picture.contentType

	    picture.ref.moveTo(new File(Play.application.path + "/public/images/carros/" + picture.filename))
	    Ok("File uploaded")
  	}.getOrElse {
  	    Redirect(routes.Application.index).flashing(
  	      "error" -> "Missing file"
  	    )
  	}
  }

  def handleUpload( request: Request[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]]) : Option[String] = {

   request.body.file("img").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType

      var f1 = new File(Play.application.path + "/public/images/carros/" + picture.filename)
      var found:Boolean = false
      if (f1.exists()) {
        f1 = new File(Play.application.path + "/public/images/carros/new-" + picture.filename)
        found = true
      }
      picture.ref.moveTo(f1)
      if (found) {
        Option("new-" + picture.filename)
      } else {
        Option(picture.filename)
      }

    }.getOrElse {
      println("Missing file.")
      /*
        Redirect(routes.Application.index).flashing(
          "error" -> "Missing file"
        )
      */
      None
    } 
  }

  def remove = TODO

  def save = TODO
}
