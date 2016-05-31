package com.sap.yaas.wishlist.model

import play.api.libs.json.Json

case class OAuthToken(token_type: String, access_token: String, expires_in: Int, scope: String)

case class OAuthTokenError(error: String, error_description: String, error_uri: String)

// TODO: write mapper for camelCase variable names
object OAuthToken {
    implicit val tokenFormat = Json.format[OAuthToken]
}

object OAuthTokenError {
  implicit val tokenErrorFormat = Json.format[OAuthTokenError]
}