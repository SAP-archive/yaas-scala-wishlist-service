package com.sap.cloud.yaas.wishlist.model

import java.net.URI

import play.api.libs.json.Json

/**
 * Provides error information for a request.
 * Defined in https://api.yaas.io/patterns/v1/schema-error-message.json.
 */
case class ErrorMessage(status: Int, `type`: String, message: String, details: Seq[ErrorDetail],
  moreInfo: URI)

object ErrorMessage {

  import com.sap.cloud.yaas.wishlist.util.UriFormat._

  implicit val errorMessageFormat = Json.format[ErrorMessage]

}
