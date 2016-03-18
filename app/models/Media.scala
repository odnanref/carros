package models

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.json._


case class Media(id:Option[Long], name:String, path:String)

