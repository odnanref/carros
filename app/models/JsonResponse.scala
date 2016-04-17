
package models

import play.api.libs.json._

case class JsonResponse(status:String, data:String) {

	implicit val JsonResponseWrites = new Writes[JsonResponse] {
	  def writes(jsonresp: JsonResponse) = Json.obj(
	    "status" -> jsonresp.status,
	    "data" -> jsonresp.data
	  )
	}
}
