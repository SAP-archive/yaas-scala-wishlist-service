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

import com.sap.yaas.wishlist.model.{ OAuthToken, OAuthTokenError }
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.ws.WSClient

import scala.concurrent.{ ExecutionContext, Future }
import akka.pattern.CircuitBreaker
import akka.actor.ActorSystem
import scala.concurrent.duration._
import play.api.Logger
import play.api.libs.ws.WSResponse

class OAuthTokenService @Inject() (config: Configuration, ws: WSClient, system: ActorSystem)(implicit context: ExecutionContext) extends OAuthTokenProvider {

  val baseUri = config.getString("yaas.security.oauth_url").get
  
  val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = config.getInt("yaas.security.oauth_max_failures").getOrElse(5),
      callTimeout = Duration(config.getMilliseconds("yaas.security.oauth_call_timeout").getOrElse(10000l),MILLISECONDS),
      resetTimeout = Duration(config.getMilliseconds("yaas.security.oauth_reset_timeout").getOrElse(60000l),MILLISECONDS))
        .onHalfOpen(notifyOnHalfOpen())
        .onOpen(notifyOnOpen())

  def notifyOnHalfOpen(): Unit =
    Logger.warn("CircuitBreaker is now half open, if the next call fails, it will be open again")

  def notifyOnOpen(): Unit =
    Logger.warn("CircuitBreaker is now open, and will not close for one minute")

  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken] = {
    val hdrs = "Content-Type" -> "application/x-www-form-urlencoded"
    var body = Map("grant_type" -> Seq(OAuthTokenService.GRANT_TYPE),
      "client_id" -> Seq(clientId),
      "client_secret" -> Seq(clientSecret),
      "scope" -> scopes)
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failEarly(ws.url(baseUri + "/token")
      .withHeaders(hdrs)
      .post(body)))
    
    futureResponse.map(
        response =>
          response.status match {
            case OK =>
              response.json.validate[OAuthToken]
                .fold(_ => throw new Exception("parse json failed on success"),
                  s => s)
            case INTERNAL_SERVER_ERROR =>
              throw new Exception(s"Service error ${response.status}: ${response.body}")
            case default =>
              response.json.validate[OAuthTokenError]
                .fold(_ => throw new Exception("parse json failed on failure"),
                  s => throw new TokenErrorException(s))
          })
  }

  def invalidateToken: Unit = {

  }
  
  def failEarly(wsresponse: Future[WSResponse]): Future[WSResponse] =
    wsresponse.map(
      response =>
        response.status match {
          /* fail ws request if we get a 503 */
          case SERVICE_UNAVAILABLE | GATEWAY_TIMEOUT | INSUFFICIENT_STORAGE => throw new Exception()
          case _ => response
        })
}

object OAuthTokenService {
  val GRANT_TYPE = "client_credentials"

}
