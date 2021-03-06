package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class Application  @Inject() (repo: CarroRepository, repomedia: MediaRepository)
                                 (implicit ec: ExecutionContext) extends Controller {

  implicit val CarroFormat = Json.format[Carro]

  def index = Action {
    Ok(views.html.index("Your new application is ready. Muhahaha", "andre"))
  }

  def carro( id:Long) = Action.async {
  	
  	//val car = new Carro(1, "BMW M6", "5 portas, de 2005", "bmw-m6.jpeg", "bmw, m6, 5 portas")
  	repo.get(id).map { car =>
      val medias = if (car != None) {
        repomedia.getByCarId(car.get.id.get.toLong)
      } else {
        Nil
      }

      if (car != None) {
        Ok(views.html.carro(
          car.getOrElse(throw new RuntimeException("None available")),
          medias.toList))
      } else {
        NotFound("Nenhum carro encontrado.")
      }
		  
      // TODO make this show a not found personalised page
  	}
  }

  def search(model: Option[Long], year: Option[Int]) = Action.async {
    if (model.isEmpty || year.isEmpty) {
      Future { Ok(views.html.search()) }
    } else {
      repo.search(model.get, year.get).map { res =>
        Ok(Json.toJson(res))
      }
    }
  }
}
