package com.sap.yaas.wishlist.oauth

import javax.inject.Inject

import com.sap.yaas.wishlist.model.{OAuthToken, OAuthTokenError}
import com.sap.yaas.wishlist.service.RemoteServiceException
import play.api.http.Status._
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}

class OAuthTokenService @Inject() (ws: WSClient)(implicit context: ExecutionContext) extends OAuthTokenProvider {
  
  val BASE_URI = "https://api.yaas.io/hybris/oauth2/v1" 
  val GRANT_TYPE = "client_credentials"
  
  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken] = {
    val hdrs = "Content-Type" -> "application/x-www-form-urlencoded"
    var body = Map("grant_type" -> Seq(GRANT_TYPE),
                   "client_id" -> Seq(clientId),
                   "client_secret" -> Seq(clientSecret),
                   "scope" -> scopes)
    ws.url(BASE_URI + "/token")
        .withHeaders(hdrs)
        .post(body)
        .map(
          response => 
            response.status match {
              case OK =>
                (response.json).validate[OAuthToken]
                    .fold(_ => throw new Exception("parse json failed on success"),
                           s => s)
              case INTERNAL_SERVER_ERROR =>
                throw new RemoteServiceException("Something went wrong")
              case default =>
                (response.json).validate[OAuthTokenError]
                    .fold(_ => throw new Exception("parse json failed on failure"),
                           s => throw new TokenErrorException(s))
            }
        )
    }
  
  def invalidateToken = {
    
  }
}