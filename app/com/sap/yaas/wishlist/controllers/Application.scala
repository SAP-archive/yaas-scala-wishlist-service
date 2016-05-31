package com.sap.yaas.wishlist.controllers

import com.google.inject.Inject
import com.sap.yaas.wishlist.document.{DocumentClient, DocumentExistsException}
import com.sap.yaas.wishlist.model.{Wishlist, WishlistItem, YaasAwareParameters}
import com.sap.yaas.wishlist.oauth.OAuthTokenService
import com.sap.yaas.wishlist.service.RemoteServiceException
import play.api.Configuration
import com.sap.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json, _}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by lutzh on 30.05.16.
  */
class Application @Inject() (documentClient: DocumentClient,
                             oauthClient: OAuthTokenCacheWrapper,
                             config: Configuration)(implicit context: ExecutionContext) extends Controller {

  def list = Action.async { request =>
    oauthClient.acquireToken(config.getString("yaas.security.client_id").get, config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj")).map(token =>
      Ok(Json.toJson(WishlistItem.dummyItem) + " + " + token.access_token)).recover({
      case _ =>
        throw new RemoteServiceException("Error during token request, please try again.")
    }
    )
  }

  def create() = Action.async(BodyParsers.parse.json) { request =>
    val jsresult: JsResult[Wishlist] = request.body.validate[Wishlist]
    jsresult match {
      case wishlistOpt: JsSuccess[Wishlist] =>
        val yaasAwareParameters: YaasAwareParameters = getYaasAwareParameters(request)
        println("wishlist: " + jsresult.get)

        (for {
          token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
                    config.getString("yaas.security.client_secret").get, Seq("hybris.document_manage"))
          result <- documentClient.create(
            yaasAwareParameters, wishlistOpt.get, token.access_token).map(
            response => Ok(Json.toJson(response))
          )
        } yield result)
          .recover({
          case e: DocumentExistsException => Conflict
          case e: Exception =>
            e.printStackTrace()
            InternalServerError(e.getMessage)
        })
      case error: JsError =>
        println("Errors: " + JsError.toJson(error).toString())
        Future(BadRequest)
    }
  }

  def getYaasAwareParameters(request: Request[JsValue]): YaasAwareParameters = {
    // TODO: validation, currently 500
    new YaasAwareParameters(
      request.headers.get("hybris-tenant").get,
      request.headers.get("hybris-client").get,
      request.headers.get("scope").getOrElse(""),
      request.headers.get("hybris-user"),
      request.headers.get("hybris-requestId"),
      request.headers.get("hybris-hop").getOrElse("1").toInt)
  }

  def update = Action(BodyParsers.parse.json) { request =>
    val jsresult: JsResult[WishlistItem] = request.body.validate[WishlistItem]
    jsresult match {
      case _: JsSuccess[WishlistItem] =>
        println("wishlist item: " + jsresult.get)
        Ok
      case error: JsError =>
        println("Errors: " + JsError.toJson(error).toString())
        BadRequest
    }
  }
}
