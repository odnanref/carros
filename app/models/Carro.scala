package models

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.json._

case class Carro(id: Option[Long], name:String, year:Int, description:String,
                 img:String, keywords:String, state: String, model: Long)

case class CarroFormData(id:String, name: String, year:Int, description: String,
                         keywords: String, state: String, model: Long)

object CarroForm {

  val form = Form(
    mapping(
      "id" -> text,
      "name" -> nonEmptyText,
      "year" -> number,
      "description" -> nonEmptyText,
      "keywords" -> nonEmptyText,
      "state"	-> nonEmptyText,
      "model" -> longNumber
    )(CarroFormData.apply)(CarroFormData.unapply)
  )
}

object Carros {
  implicit val carroFormat = Json.format[Carro]
}