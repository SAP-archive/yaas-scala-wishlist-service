package com.sap.cloud.yaas.wishlist.oauth

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.sap.cloud.yaas.wishlist.com.sap.cloud.yaas.wishlist.config.Config
import com.sap.cloud.yaas.wishlist.model.{OAuthToken, OAuthTokenError}
import com.sap.cloud.yaas.wishlist.security.Credentials
import com.sap.cloud.yaas.wishlist.util.WSHelper._
import com.sap.cloud.yaas.wishlist.util.YaasLogger
import play.api.http.Status._
import play.api.http.{ContentTypes, HeaderNames}
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

/**
  * Client for accessing the OAuth2 service to request tokens for authenticated use of APIs.
  */
class OAuthTokenService @Inject()(config: Config, ws: WSClient, system: ActorSystem)(
  implicit context: ExecutionContext) extends OAuthTokenProvider {

  val logger = YaasLogger(this.getClass)
  val baseUri = config.baseUri

  val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = config.oauthMaxFailures,
      callTimeout = config.oauthCallTimeout,
      resetTimeout = config.oauthResetTimeout)
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
    * @param scopes requested scopes for the request token
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

}

object OAuthTokenService {

  val GRANT_TYPE = "client_credentials"

}
