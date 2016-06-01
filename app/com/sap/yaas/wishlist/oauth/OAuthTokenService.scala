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
package com.sap.yaas.wishlist.oauth

import javax.inject.Inject

import com.sap.yaas.wishlist.model.{OAuthToken, OAuthTokenError}
import com.sap.yaas.wishlist.service.RemoteServiceException
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OAuthTokenService @Inject()(configuration: Configuration, ws: WSClient)(implicit context: ExecutionContext) extends OAuthTokenProvider {

  val baseUri = configuration.getString("yaas.security.oauth_url").get

  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken] = {
    val hdrs = "Content-Type" -> "application/x-www-form-urlencoded"
    var body = Map("grant_type" -> Seq(OAuthTokenService.GRANT_TYPE),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "scope" -> scopes)
    ws.url(baseUri + "/token")
      .withHeaders(hdrs)
      .post(body)
      .map(
        response =>
          response.status match {
            case OK =>
              response.json.validate[OAuthToken]
                .fold(_ => throw new Exception("parse json failed on success"),
                  s => s)
            case INTERNAL_SERVER_ERROR =>
              throw new RemoteServiceException("Something went wrong")
            case default =>
              response.json.validate[OAuthTokenError]
                .fold(_ => throw new Exception("parse json failed on failure"),
                  s => throw new TokenErrorException(s))
          }
      )
  }

  def invalidateToken: Unit = {

  }
}

object OAuthTokenService {
  val GRANT_TYPE = "client_credentials"

}
