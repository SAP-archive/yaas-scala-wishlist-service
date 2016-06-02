/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */
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
