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
import com.sap.yaas.wishlist.model.Wishlist
import com.sap.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import com.sap.yaas.wishlist.security.{Credentials, YaasActions}
import com.sap.yaas.wishlist.service.ConstraintViolationException
import com.sap.yaas.wishlist.util.{ErrorMapper, YaasLogger}
import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json, _}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import Application._

class Application @Inject()(documentClient: DocumentClient,
                            oauthClient: OAuthTokenCacheWrapper, errorMapper: ErrorMapper,
                            config: Configuration, yaasActions: YaasActions)(implicit context: ExecutionContext) extends Controller {

  val logger = YaasLogger(this.getClass)

  val credentials = Credentials(config.getString("yaas.security.client_id").get, config.getString("yaas.security.client_secret").get)

  import yaasActions._

  def getAll(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_VIEW))
      result <- documentClient.getAll(token.access_token, pageNumber, pageSize).map(response =>
        Ok(Json.toJson(response)))
    } yield result
  }


  def create(): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    request.body.validate[Wishlist] match {
      case JsSuccess(jsonWishlist, _) =>
        logger.debug("wishlist: " + jsonWishlist)
        for {
          token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
          result <- documentClient.create(jsonWishlist, token.access_token).map(
            response => Ok(Json.toJson(response)))
        } yield result
      case JsError(errors) =>
        Future.failed(new ConstraintViolationException(errors.map({ case (path, errlist) => (path.toString, errlist.map(_.message)) })))
    }
  }

  def update(wishlistId: String): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    request.body.validate[Wishlist] match {
      case JsSuccess(jsonWishlist, _) =>
        logger.debug("wishlist item: " + jsonWishlist)
        for {
          token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
          result <- documentClient.update(jsonWishlist, token.access_token).map(
            response => Ok(Json.toJson(response)))
        } yield result
      case JsError(errors) =>
        Future.failed(new ConstraintViolationException(errors.map({ case (path, errlist) => (path.toString, errlist.map(_.message)) })))
    }
  }

  def delete(wishlistId: String): Action[AnyContent] = ManageAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
      result <- documentClient.delete(wishlistId, token.access_token).map(response =>
        NoContent)
    } yield result
  }

  def get(wishlistId: String): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_VIEW))
      result <- documentClient.get(wishlistId, token.access_token).map(response =>
        Ok(Json.toJson(response)))
    } yield result
  }
}

object Application {

  val SCOPE_DOCUMENT_MANAGE = "hybris.document_manage"
  
  val SCOPE_DOCUMENT_VIEW = "hybris.document_view"

}
