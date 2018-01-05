package models

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._
import java.time.ZoneId
import java.sql.Timestamp
import java.text.SimpleDateFormat

case class Carro(id: Option[Long], name:String, year:Int, description:String,
                 img:String, keywords:String, state: String, model: Long,
                 datein: DateTime)

case class CarroFormData(id:String, name: String, year:Int, description: String,
                         keywords: String, state: String, model: Long, datein: String)

object CarroForm {

  val form = Form(
    mapping(
      "id" -> text,
      "name" -> nonEmptyText,
      "year" -> number,
      "description" -> nonEmptyText,
      "keywords" -> nonEmptyText,
      "state"	-> nonEmptyText,
      "model" -> longNumber,
      "datein" -> text
    )(CarroFormData.apply)(CarroFormData.unapply)
  )
}

object Carros {

  implicit val carroFormat = Json.format[Carro]

  implicit def convertTimestampToDate(timestamp: Timestamp) : DateTime = {
    val ins = timestamp.toLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()
    new DateTime(ins)
  }

  implicit def convertDateToTimestamp(dt : DateTime) : Timestamp = {
    new Timestamp(dt.getMillis())
  }

  implicit def convertTextToDateTime(txt: String) : DateTime =  {
    val sdf = new SimpleDateFormat("dd/MM/yyyy H:m:s")
    val inst = sdf.parse(txt).toInstant
    new DateTime(inst.toEpochMilli)
  }

  implicit def convertDateTimeToText(dt : DateTime) : String = {
    val sdf = new SimpleDateFormat("yyyy-MM-dd H:i:s");
    sdf.format(dt.getMillis())
  }
}