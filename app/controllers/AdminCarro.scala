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

import java.io.File

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
  	
  	val car = new Carro(Some(1), "BMW M6", "5 portas, de 2005", "bmw-m6.jpeg", "bmw, m6, 5 portas", "active")
  	Ok(views.html.admin.carro(car))
  }

  def addView = Action {
    Ok(views.html.admin.index(CarroForm.form))
  }

  def save() = Action.async(parse.multipartFormData) { implicit request =>
    
    val filename = handleUpload(request)
    println("filename " + filename)
    CarroForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.index(errorForm))
      },
      carro => {
        repo.insert(new Carro(None, carro.name, carro.description, filename.getOrElse("logo.png"), carro.keywords, carro.state))
        /*
        repo.create(carro.name, carro.description, filename.getOrElse("logo.png"), carro.keywords, carro.state)
        */
        .map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.Application.index)
        }
        
      }
    )
  }

  def update() = Action.async(parse.multipartFormData) { implicit request =>
    //val tId:Long = id.toLong
    val filename = handleUpload(request)

    CarroForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => scala.concurrent.Future {
        Ok(views.html.admin.index(errorForm))
      },
      carro => {        
        val car = new Carro( Some(carro.id.toLong), carro.name, carro.description, 
          filename.getOrElse(repo.getImage(carro.id.toLong)), carro.keywords, carro.state)

        repo.edit(carro.id.toLong, car).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminCarro.index)
        }
      }
    )
  }

  def editView(id:Long) = Action.async {
    //Ok(views.html.admin.index(CarroForm.form))

    //val car = new Carro(1, "BMW M6", "5 portas, de 2005", "bmw-m6.jpeg", "bmw, m6, 5 portas")
    repo.get(id).map { car =>
      // TODO better 404
      if (car == None) {
        NotFound
      } else {

      val data = Map(
        "id" -> car.get.id.get.toString, 
        "name" -> car.get.name,
        "description" -> car.get.description,
        "keywords" -> car.get.keywords,
        "img" -> car.get.img,
        "state" -> car.get.state
        )
      Ok(views.html.admin.index(CarroForm.form.bind(data)))
      }
      // TODO make this show a not found personalised page
    }
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
      val imagePath = current.configuration.getString("car.imageLocation").getOrElse("")

      var f1 = new File(Play.application.path + imagePath + picture.filename)
      
      val found:Boolean = if (f1.exists()) {
        f1 = new File(Play.application.path + imagePath + "new-" + picture.filename)
        true
      } else {
        false
      }

      picture.ref.moveTo(f1) 
      // FIXME handle move error
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

  /**
   * Delete existing img file by the Car ID
   *
   * @param int Id
   *
   */
  def removeImage(id: Long) = Action {
    var json = ""

    val car = repo.get(id).map { car =>
      car.getOrElse(throw new RuntimeException("None available")) // TODO better 404
      // FIXME place complete path for the image file
      val imagePath = current.configuration.getString("car.imageLocation").getOrElse("")
      val imageName = Play.application.path + imagePath + car.get.img
      val f1 = new File(imageName)
      
      if (imageName.toUpperCase != "" && f1.exists()) {
        f1.delete()
        if (!f1.exists()) {
          repo.clearImage(id) // set to empty in database
          json = "{status:\"ok\", data: \"Imagem removida\"}" // image deleted ok
        } else {
          json = "{status:\"error\", data: \"Imagem não removida\"}" // image deleted error
        }
      }
      
    }
    Ok(json)
  }

  def remove = TODO

}
