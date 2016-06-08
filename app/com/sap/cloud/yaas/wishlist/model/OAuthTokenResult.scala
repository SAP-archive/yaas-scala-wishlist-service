package com.sap.cloud.yaas.wishlist.model

import play.api.libs.json.Json

/**
 * Case class to provide OAuth token format, retrieved by the API as json
 */
case class OAuthToken(token_type: String, access_token: String, expires_in: Int, scope: String)

/**
 * Case class to provide OAuth token error format, retrieved by the API on failure as json
 */
case class OAuthTokenError(error: String, error_description: String, error_uri: String)

object OAuthToken {
  implicit val tokenFormat = Json.format[OAuthToken]
}

object OAuthTokenError {
  implicit val tokenErrorFormat = Json.format[OAuthTokenError]
}
