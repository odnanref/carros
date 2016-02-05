package models

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.json._

case class Carro(id: Long, name:String, description:String, img:String, keywords:String)

case class CarroFormData(name: String, description: String, keywords: String)

object CarroForm {

  val form = Form(
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "keywords" -> nonEmptyText
    )(CarroFormData.apply)(CarroFormData.unapply)
  )
}

object Carros {


  implicit val carroFormat = Json.format[Carro]

}