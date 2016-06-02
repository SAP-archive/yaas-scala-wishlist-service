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

import com.sap.yaas.wishlist.model.OAuthToken
import play.api.cache.CacheApi

import scala.concurrent.{ ExecutionContext, Future }

class OAuthTokenCacheWrapper @Inject() (wrappedTokenService: OAuthTokenService,
  cache: CacheApi)(implicit context: ExecutionContext)
    extends OAuthTokenProvider {

  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken] = {
    cache.get[OAuthToken](scopes.mkString(" ")) match {
      case Some(token) =>
        Future.successful(token)
      case None =>
        wrappedTokenService.acquireToken(clientId, clientSecret, scopes).map { token =>
          cache.set(scopes.mkString(" "), token)
          token
        }

    }
  }

  def invalidateToken: Unit = {

  }
}

object OAuthTokenCacheWrapper {
  val DEFAULT_MAX_CACHE_SIZE = 1000
  val DEFAULT_EAGER_EXPIRATION_OFFSET = 5
}
