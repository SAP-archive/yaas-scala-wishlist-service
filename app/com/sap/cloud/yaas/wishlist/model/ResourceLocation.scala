package com.sap.cloud.yaas.wishlist.model

import play.api.libs.json.Json

/**
 * ResourceLocation holding the info of a created document in document service
 */
case class ResourceLocation(id: String, link: String)

object ResourceLocation {
  implicit val ResourceLocationFormat = Json.format[ResourceLocation]

}
