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
package com.sap.yaas.wishlist.controllers

import com.google.inject.Inject
import com.sap.yaas.wishlist.document.{DocumentClient, DocumentExistsException}
import com.sap.yaas.wishlist.model.{Wishlist, WishlistItem}
import com.sap.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import com.sap.yaas.wishlist.security.YaasActions._
import com.sap.yaas.wishlist.service.RemoteServiceException
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json, _}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

class Application @Inject()(documentClient: DocumentClient,
                            oauthClient: OAuthTokenCacheWrapper,
                            config: Configuration)(implicit context: ExecutionContext) extends Controller {

  def getWishlists(pageNumber: Int, pageSize: Int): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    val pageNo = if (pageNumber > 0) Some(pageNumber) else None
    val pageS = if (pageSize > 0) Some(pageSize) else None
    (for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
                config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj hybris.document_view"))
      result <- documentClient.getWishlists(token.access_token, pageNo, pageS).map(response =>
        Ok(Json.toJson(response))
      )
    } yield result)
      .recover({
        case e: Exception =>
          Logger.error("Unexpected error while reading all wishlists from doc repo", e)
          InternalServerError(e.getMessage)
      })
  }

  def create(): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
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
        Logger.debug("Errors: " + JsError.toJson(error).toString())
        Future.successful(BadRequest)
    }
  }

  def update(wishlistId: String): Action[JsValue] = ManageAction (BodyParsers.parse.json) { request =>
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
  
  def delete(wishlistId: String): Action[AnyContent] = ManageAction.async { request =>
    implicit val yaasContext = request.yaasContext
    (for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
                config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj hybris.document_view"))
      result <- documentClient.delete(wishlistId, token.access_token).map(response =>
        Ok("called document service with delete" + response)
      )
    } yield result)
      .recover({
        case e: Exception =>
          Logger.error("Unexpected error while reading all wishlists from doc repo", e)
          InternalServerError(e.getMessage)
      })
  }
  
  def getWishlist(wishlistId: String): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    (for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
                config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj hybris.document_view"))
      result <- documentClient.getWishlist(wishlistId, token.access_token).map(response =>
        Ok(Json.toJson(response))
      )
    } yield result)
      .recover({
        case e: Exception =>
          Logger.error("Unexpected error while reading all wishlists from doc repo", e)
          InternalServerError(e.getMessage)
      })
  }
}
