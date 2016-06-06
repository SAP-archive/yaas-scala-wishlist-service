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

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.sap.yaas.wishlist.model.{OAuthToken, OAuthTokenError}
import com.sap.yaas.wishlist.security.Credentials
import com.sap.yaas.wishlist.util.{WSHelper, YaasLogger}
import play.api.Configuration
import play.api.http.{ContentTypes, HeaderNames}
import play.api.http.Status._
import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import WSHelper._

/**
 * Client for accessing the OAuth2 service to request tokens for authenticated use of APIs.
 */
class OAuthTokenService @Inject()(config: Configuration, ws: WSClient, system: ActorSystem)(
  implicit context: ExecutionContext) extends OAuthTokenProvider {

  val baseUri = config.getString("yaas.security.oauth_url").get
  val logger = YaasLogger(this.getClass)

  val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = config.getInt("yaas.security.oauth_max_failures").get,
      callTimeout = Duration(config.getMilliseconds("yaas.security.oauth_call_timeout").get, MILLISECONDS),
      resetTimeout = Duration(config.getMilliseconds("yaas.security.oauth_reset_timeout").get, MILLISECONDS))
      .onHalfOpen(notifyOnHalfOpen())
      .onOpen(notifyOnOpen())

  /**
   * Event endpoint for circuit breaker half open event
   */
  def notifyOnHalfOpen(): Unit =
    logger.getLogger.warn("CircuitBreaker is now half open, if the next call fails, it will be open again")

  /**
   * Event endpoint for circuit breaker open event
   */
  def notifyOnOpen(): Unit =
    logger.getLogger.warn("CircuitBreaker is now open, and will not close for one minute")

  /**
   * Queries the OAuth2 service, requesting a new token with given scopes and credentials
   * @param credentials to be used for the token request
   * @param scope for the request token
   * @return a Future[OAuthToken]
   */
  def acquireToken(credentials: Credentials, scopes: Seq[String]): Future[OAuthToken] = {
    import credentials._
    val hdrs = HeaderNames.CONTENT_TYPE -> ContentTypes.FORM
    val body = Map("grant_type" -> Seq(OAuthTokenService.GRANT_TYPE),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "scope" -> scopes)
    breaker.withCircuitBreaker(failFast(ws.url(baseUri + "/token")
      .withHeaders(hdrs)
      .post(body)))
      .map(
      response =>
        response.status match {
          case OK =>
            response.json.validate[OAuthToken]
              .fold(_ => throw new Exception("parse json failed on success"),
                s => s)
          case INTERNAL_SERVER_ERROR =>
            throw new Exception(s"Service error ${response.status}: ${response.body}")
          case _ =>
            response.json.validate[OAuthTokenError]
              .fold(_ => throw new Exception("parse json failed on failure"),
                s => throw new TokenErrorException(s))
        })
  }

  /**
   * UNIMPLEMENTED: Future implementation should cover invalidating tokens. Check in token acquisition if
   * a token is about to expire and then rather request a new one and invalidate the old one, instead of
   * providing the old one as result.
   */
  def invalidateToken: Unit = ???

}

object OAuthTokenService {
  val GRANT_TYPE = "client_credentials"

}
