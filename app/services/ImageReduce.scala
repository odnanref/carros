
package services

import java.io.File
import java.io.FileNotFoundException
import java.io.FileInputStream
import java.io.FileOutputStream

import com.sksamuel.scrimage.ScaleMethod._
import com.sksamuel.scrimage._

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// need the image
// image path
// new location will be based on the car_id
	
class ImageReduce(path: String, car_id:Long) {

	val Logger:Logger = LoggerFactory.getLogger(this.toString)

	if (!new File(path).exists()) {
		Logger.info("Trying to scale image but not found the path " + path )
		throw new FileNotFoundException("Trying to scale image but not found: " + path )
	}

	val basename:String = if (path.indexOf("/") >= 0 ) {
		path.split("/").last
	} else { // no path just the file
		path // assuming its just the filename
	}
	Logger.debug(" Filename and basename " + basename + " from path " + path )
	val newFilename = getNewPath() + File.separator + basename.split('.')(0) + "-small" + 
	basename.split('.')(1)

	Logger.info("new file is " + newFilename)

	def scale() {
		Logger.info("Prep for scaling the image " + path )
		if (createDirectory()) { // create the new structure directory if needed
			Logger.info("directory created for " + path )
			val in = new FileInputStream(path) // input stream
			//val out = new FileOutputStream(newFilename) // output stream
			Image.fromStream(in).scale(0.5, Bicubic).output(newFilename) 
			Logger.info("Image scaled " + path)
		}
	}
	
	/**
	 * Create the directory for the new file resized location
	 * it will be based on the car_id
	 *
	 * @return Boolean
	 */
	def createDirectory() : Boolean = {
		if (new File(getNewPath()).exists()) {
			true
		} else {
			new File(getNewPath()).mkdirs()
		}
	}

	/**
	 * Get the new path based on the current path
	 *
	 * @return String
	 */
	def getNewPath() : String = {
		if (path.indexOf("/") != -1 ) {
			path.substring(0, path.lastIndexOf("/")) + File.separator + 
			car_id + File.separator
		} else {
			"." + File.separator + car_id + File.separator
		}
	}
}