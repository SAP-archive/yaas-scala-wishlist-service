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
import com.sap.yaas.wishlist.document.DocumentClient
import com.sap.yaas.wishlist.model.{Wishlist, WishlistItem}
import com.sap.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import com.sap.yaas.wishlist.security.YaasActions._
import com.sap.yaas.wishlist.service.ConstraintViolationException
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json, _}
import play.api.mvc._
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.CircuitBreaker
import scala.concurrent.duration._
import akka.actor.ActorSystem
import com.sap.yaas.wishlist.model.OAuthToken


class Application @Inject()(documentClient: DocumentClient,
                            oauthClient: OAuthTokenCacheWrapper,
                            config: Configuration, system: ActorSystem)(implicit context: ExecutionContext) extends Controller {
  
   val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = 5,
      callTimeout = 10.seconds,
      resetTimeout = 1.minute).onOpen(notifyMeOnOpen())
 
  def notifyMeOnOpen(): Unit =
    Logger.warn("My CircuitBreaker is now open, and will not close for one minute")

  
  def getWishlists(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
        config.getString("yaas.security.client_secret").get, Seq("hybris.document_view"))
      result <- documentClient.getWishlists(token.access_token, pageNumber, pageSize).map(response =>
        Ok(Json.toJson(response))
      )
    } yield result
  }

  def create(): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    request.body.validate[Wishlist] match {
      case JsSuccess(jsonWishlist, _) =>
        Logger.debug("wishlist: " + jsonWishlist)
        for {
          token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
            config.getString("yaas.security.client_secret").get, Seq("hybris.document_manage"))
          result <- documentClient.create(jsonWishlist, token.access_token).map(
            response => Ok(Json.toJson(response))
          )
        } yield result
      case JsError(errors) =>
        Future.failed(new ConstraintViolationException(errors.map({ case (path, errlist) => (path.toString, errlist) })))
    }
  }

  def update(wishlistId: String): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
      request.body.validate[Wishlist] match {
        case JsSuccess(jsonWishlist, _) =>
          Logger.debug("wishlist item: " + jsonWishlist)
          Future.successful(Ok)
        case JsError(errors) =>
          Future.failed(new ConstraintViolationException(errors.map({ case (path, errlist) => (path.toString, errlist) })))
    }
  }

  def delete(wishlistId: String): Action[AnyContent] = ManageAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
        config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj hybris.document_view"))
      result <- documentClient.delete(wishlistId, token.access_token).map(response =>
        Ok("called document service with delete" + response)
      )
    } yield result
  }

  def getWishlist(wishlistId: String): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(config.getString("yaas.security.client_id").get,
        config.getString("yaas.security.client_secret").get, Seq("hybris.tenant=altoconproj hybris.document_view"))
      result <- documentClient.getWishlist(wishlistId, token.access_token).map(response =>
        Ok(Json.toJson(response))
      )
    } yield result
  }
}
