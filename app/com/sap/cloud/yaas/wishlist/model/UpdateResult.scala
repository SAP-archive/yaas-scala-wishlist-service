package com.sap.cloud.yaas.wishlist.model

import play.api.libs.json.Json

/**
 * Case class for providing the format to parse PUT request messages
 */
case class UpdateResult(code: String, status: Option[String] = None, message: Option[String] = None, data: Option[String] = None)

object UpdateResult {
  implicit val UpdateResultFormat = Json.format[UpdateResult]

}
