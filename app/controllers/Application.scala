package controllers

import play.api._
import play.api.mvc._
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class Application  @Inject() (repo: CarroRepository)
                                 (implicit ec: ExecutionContext) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready. Muhahaha", "andre"))
  }

  def carro( id:Long) = Action.async {
  	
  	//val car = new Carro(1, "BMW M6", "5 portas, de 2005", "bmw-m6.jpeg", "bmw, m6, 5 portas")
  	repo.get(id).map { car =>
		  Ok(views.html.carro(car.getOrElse(throw new RuntimeException("None available"))))
      // TODO make this show a not found personalised page
  	}
  }  
}
