package models

import play.api.data.Form
import play.api.data.Forms._

import play.api.libs.json._

import play.api.Play
import play.api.Play.current // for getting the current application path

import java.io.File

case class Media(id:Option[Long], name:String, path:String, car_id:Long) {
	/**
	 * Small image version
	 */
	val small:String = car_id.toString + File.separator + 
		path.split('.')(0) + "-small" + '.' + path.split('.')(1)
}

object Media {

	val imagePath = current.configuration.getString("car.imageLocation").getOrElse("")
	
	def removePhiMedia(media: Media) :Boolean = {	
     	val imageName = Play.application.path + imagePath + media.path
     	val f1 = new File(imageName)
     	if (f1.exists()) {
     		f1.delete()
     		if (media.car_id > 0 ) {
     			removeSmall(media)
     		} else {
     			true
     		}
     	} else {
     		true // there was nothing to delete so success was iminent
     	}
	}

	def removeSmall(media: Media) :Boolean = {
		require(media.car_id > 0)

		val imageName = Play.application.path + imagePath
		val fpath = imageName + File.separator + media.car_id.toString  + File.separator + 
			media.path.split('.')(0) + "-small" + media.path.split('.')(1)
		val f1 = new File(fpath)
		if (f1.exists()) {
			f1.delete()
		} else {
			true
		}
	}
}

