package com.sap.yaas.wishlist.oauth

import javax.inject.Inject

import play.api.mvc._
import play.api.libs.ws.WSClient
import play.api.libs.json._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import com.sap.yaas.wishlist.model.{OAuthToken, OAuthTokenError}

import play.api.http.Status._
import org.apache.http.ParseException
import com.sap.yaas.wishlist.service.RemoteServiceException

class OAuthTokenService @Inject() (ws: WSClient)(implicit context: ExecutionContext) {
  
  val BASE_URI = "https://api.yaas.io/hybris/oauth2/v1" 
  val GRANT_TYPE = "client_credentials"
  
  
  def getToken: Future[OAuthToken] = {
    val hdrs = "Content-Type" -> "application/x-www-form-urlencoded"
    val jsonBody: JsValue = JsObject(Seq("grant_type" -> JsString(GRANT_TYPE),
                                         "client_id" -> JsString("Vt6BP2AsBGZsWH2UIe9rEi54d1MWWKUC"),
                                         "client_secret" -> JsString("1u95IqAhTXP7rnmG"),
                                         "scope" -> JsString("")))
    
    ws.url(BASE_URI + "/token")
        .withHeaders(hdrs)
        .withBody(Json.toJson(jsonBody))
        .get()
        .map(
          response => 
            response.status match {
              case OK =>
                (response.json).validate[OAuthToken]
                    .fold(_ => throw new ParseException("parse json failed on success"),
                           s => s)
              case INTERNAL_SERVER_ERROR =>
                throw new RemoteServiceException("Something went wrong")
              case default =>
                (response.json).validate[OAuthTokenError]
                    .fold(_ => throw new ParseException("parse json failed on failure"),
                           s => throw new TokenErrorException(s))
            }
        )
    }
}