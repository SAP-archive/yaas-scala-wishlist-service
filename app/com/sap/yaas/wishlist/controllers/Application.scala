package com.sap.yaas.wishlist.controllers

import com.google.inject.Inject
import com.sap.yaas.wishlist.document.{DocumentClient, DocumentExistsException}
import com.sap.yaas.wishlist.model.{Wishlist, WishlistItem}
import com.sap.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import com.sap.yaas.wishlist.security.{ManageActionFilter, ViewActionFilter}
import com.sap.yaas.wishlist.security.YaasActions._
import com.sap.yaas.wishlist.service.RemoteServiceException
import play.api.{Configuration, Logger}
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class Application @Inject()(documentClient: DocumentClient,
                            oauthClient: OAuthTokenCacheWrapper,
                            config: Configuration)(implicit context: ExecutionContext) extends Controller {

  def list = (YaasAction andThen ViewActionFilter).async { request =>
    oauthClient.acquireToken(config.getString("yaas.security.client_id").get, config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj")).map(token =>
      Ok(Json.toJson(WishlistItem.dummyItem) + " + " + token.access_token)
    ).recover({
      case _ =>
        throw new RemoteServiceException("Error during token request, please try again.")
    })
  }

  def create() = (YaasAction andThen ManageActionFilter).async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    val jsresult: JsResult[Wishlist] = request.body.validate[Wishlist]
    jsresult match {
      case wishlistOpt: JsSuccess[Wishlist] =>
        Logger.debug("wishlist: " + jsresult.get)

        (for {
          token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
            config.getString("yaas.security.client_secret").get, Seq("hybris.document_manage"))
          result <- documentClient.create(wishlistOpt.get, token.access_token).map(
            response => Ok(Json.toJson(response))
          )
        } yield result)
          .recover({
            case e: DocumentExistsException => Conflict
            case e: Exception =>
              Logger.error("Unexpected error while creating a wishlist", e)
              InternalServerError(e.getMessage)
          })
      case error: JsError =>
        println("Errors: " + JsError.toJson(error).toString())
        Future.successful(BadRequest)
    }
  }


  def update = (YaasAction andThen ManageActionFilter) (BodyParsers.parse.json) { request =>
    val jsresult: JsResult[WishlistItem] = request.body.validate[WishlistItem]
    jsresult match {
      case _: JsSuccess[WishlistItem] =>
        Logger.debug("wishlist item: " + jsresult.get)
        Ok
      case error: JsError =>
        Logger.debug("Errors: " + JsError.toJson(error).toString())
        BadRequest
    }
  }
}
