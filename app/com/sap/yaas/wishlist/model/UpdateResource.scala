package com.sap.yaas.wishlist.model

import play.api.libs.json.Json

/**
 * Case class for providing the format to parse PUT request messages
 */
case class UpdateResource(code: String, status: Option[String] = None, message: Option[String] = None, data: Option[String] = None)

object UpdateResource {
  implicit val UpdateResourceFormat = Json.format[UpdateResource]

}
