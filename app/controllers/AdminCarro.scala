package controllers

import play.api._
import play.api.Play
import play.api.mvc._
import play.api.i18n._
import play.api.Play.current
import play.api.libs.json.Json
import models._
import dal._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import java.io.File
import javax.inject._

import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import services.Authenticated

class AdminCarro @Inject() (repo: CarroRepository, repomedia: MediaRepository,
                            repomodel: ModeloRepository,
                            val messagesApi: MessagesApi)
                           (implicit ec: ExecutionContext) extends Controller with I18nSupport {

  implicit val responseFormat = Json.format[JsonResponse]
  implicit val MediaFormat = Json.format[Media]

  def index(page: Int = 1) = Authenticated.async {
    val lista = repo.list(page)
    lista.flatMap( i =>
      repo.count().map { f =>
        Ok(views.html.admin.list(i,
          Math.ceil(f.toDouble/repo.PAGESIZE.toDouble).toInt )
        )
      }
    )
  }

  def carro( id:Long) = Authenticated.async {
    repo.get(id).map { car =>
      if (car != None) {
        Ok(views.html.admin.carro(car.get))
      } else {
        NotFound("Não vi por aqui.")
      }
    }
  }

  def addView = Authenticated {
    Ok(views.html.admin.add(CarroForm.form))
  }

  def save() = Authenticated.async(parse.multipartFormData) { implicit request =>
    val log = LoggerFactory.getLogger("TESTING")
    log.debug(" LOOK HERE SERVERD " + new java.util.Date().getTime)
    CarroForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => {
        log.debug(" LOOK HERE SERVERD " + new java.util.Date().getTime)
        Future { Ok(views.html.admin.add(errorForm)) }
      },
      carro => {
        repo.insert(new Carro(None, carro.name, carro.year, carro.description, "logo.png",
          carro.keywords, carro.state, carro.model, new DateTime()))
        .map { car =>
          val filename = handleUpload(request, car)
          if (!filename.isEmpty) {
            repo.updateMainImage(filename.get, car)
          }
          Redirect(routes.AdminCarro.editView(car))
        }
      }
    )
  }

  def update() = Authenticated.async(parse.multipartFormData) { implicit request =>
    val car_id = getCarIdFromRequest(request)
    val filename = handleUpload(request, car_id)

    CarroForm.form.bindFromRequest.fold(
      // if any error in submitted data
      errorForm => {
        val id = if (CarroForm.form("id").value != None ) {
          CarroForm.form("id").value.get.toLong
        } else {
          0
        }

        val medias = repomedia.getByCarId(id)
        repomodel.get(CarroForm.form("model").value.get.toLong).map {
          m => Ok(views.html.admin.update(errorForm, medias, m.get))
        }
      }
      ,
      carro => {
        import models.Carros._
        val car = new Carro( Some(carro.id.toLong), carro.name, carro.year, carro.description,
          filename.getOrElse(repo.getImage(carro.id.toLong)), carro.keywords, carro.state, carro.model, carro.datein)

        repo.edit(carro.id.toLong, car).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AdminCarro.editView(carro.id.toLong))
        }
      }
    )
  }

  def editView(id:Long) = Authenticated.async {

    repo.get(id).flatMap { car =>
      // TODO better 404
      if (car == None) {
        scala.concurrent.Future(NotFound)
      } else {

        val data = Map(
          "id" -> car.get.id.get.toString,
          "name" -> car.get.name,
          "year" -> car.get.year.toString(),
          "description" -> car.get.description,
          "keywords" -> car.get.keywords,
          "img" -> car.get.img,
          "state" -> car.get.state,
          "model" -> car.get.model.toString(),
          "datein" -> car.get.datein.toString("dd/MM/yyyy H:m:s")
          )
        val id = car.get.id.get.toLong
        val medias = repomedia.getByCarId(id)
        repomodel.get(car.get.model).map {
          model => Ok(views.html.admin.update(
            CarroForm.form.bind(data), medias, model.get)
          )
        }
      }
      // TODO make this show a not found personalised page
    }
  }

  def uploadView = Authenticated { request =>
    Ok(views.html.admin.upload())
  }

  def upload = Authenticated(parse.multipartFormData) { request =>
  	request.body.file("img").map { picture =>
      val car_id = getCarIdFromRequest(request)
      val filename = handleUpload(request, car_id)
	    if (filename != None) {
        val Media = new Media(None, filename.get, filename.get, car_id)
        repomedia.insert(Media)
        //repomedia.create(filename.get, filename.get, car_id)
        //Ok("{ status:\"ok\", data: \"File uploaded\"}")
        val jr = new JsonResponse("ok", "File uploaded")
        //val json = jr.responseFormat
        val json = Json.toJson(jr)
        Ok(json)
      } else {
        val jr = new JsonResponse("error", "Failed to upload")
        Ok(Json.toJson(jr))
      }
  	}.getOrElse {
        val json = new JsonResponse("error", "Failed, no file placed for upload")
        Ok(Json.toJson(json))
  	}
  }

  def handleUpload( request: Request[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]], 
      car_id:Long ) : Option[String] = {
    request.body.file("img").map { picture =>

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
      // after moving resize ...
      Future {
        val imageReduce = new services.ImageReduce(f1.getAbsolutePath(), car_id)
        imageReduce.scale()
      }
      // FIXME handle move error
      if (found) {
        Option("new-" + picture.filename)
      } else {
        Option(picture.filename)
      }

    }.getOrElse {
      println("Missing file.")

      None
    }
  }

  /**
   * Delete existing img file by the Car ID
   *
   * @param int Id
   *
   */
  def removeImage(id: Long) = Authenticated {
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
          // image deleted ok
          val json = new JsonResponse("ok", "Imagem removida")
          Ok(Json.toJson(json))
        } else {
          val json = new JsonResponse("error", "Imagem não removida")
          // image deleted error
          Ok(Json.toJson(json))
        }
      }
      
    }
    Ok(json)
  }

  /**
    * Remove the item from the database
    *
    * @param id
    * @return
    */
  def remove(id:Long) = Authenticated {
    repo.remove(id)
    val json = new JsonResponse("success", "Deleted from database.")
    Ok(Json.toJson(json))
  }

  /**
    * Remove Media from the Car
    *
    * @param id
    * @return
    */
  def removeMedia(id: Long) = Authenticated.async {
    repomedia.get(id).map { media =>
      media.getOrElse(NotFound("This was not found. Sorry.")) // TODO better 404
      if (Media.removePhiMedia(media.get)) {
        // image deleted ok
        repomedia.erase(id) // set to empty in database
        val json = new JsonResponse("ok", "Media removida")
        Ok(Json.toJson(json))
      } else {
        // image deleted error
        val json = new JsonResponse("error", "Media não removida")
        Ok(Json.toJson(json))
      }
    }
  }

  def getMediaByCarId(id:Long) = Authenticated {
    Ok(
      Json.toJson(repomedia.getByCarId(id))
    )
  }

  def getCarIdFromRequest(request: play.api.mvc.Request[play.api.mvc.MultipartFormData[play.api.libs.Files.TemporaryFile]]) :Long = {
    val car_id: Option[Seq[String]] = request.body.dataParts.get("car_id")
    if (car_id == None) {
      0
    } else {
      car_id.get(0).toLong
    }
  }

  def searchAction() = TODO

}
