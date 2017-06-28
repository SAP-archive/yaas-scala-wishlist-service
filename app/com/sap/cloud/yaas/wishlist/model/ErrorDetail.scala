package com.sap.cloud.yaas.wishlist.model

import java.net.URI

import play.api.libs.json.Json

/**
 * Provides error detail information for a specific field.
 * Defined in https://pattern.yaas.io/v1/schema-error-message.json.
 */
case class ErrorDetail(field: Option[String] = None, `type`: String, message: String,
  moreInfo: URI)

object ErrorDetail {

  import com.sap.cloud.yaas.wishlist.util.UriFormat._

  implicit val errorDetailFormat = Json.format[ErrorDetail]

}
