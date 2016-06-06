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
import com.sap.yaas.wishlist.security.Credentials
import play.api.cache.CacheApi

import scala.concurrent.{ExecutionContext, Future}

class OAuthTokenCacheWrapper @Inject() (wrappedTokenService: OAuthTokenService,
  cache: CacheApi)(implicit context: ExecutionContext)
    extends OAuthTokenProvider {

  def acquireToken(credentials: Credentials, scopes: Seq[String]): Future[OAuthToken] = {
    cache.get[OAuthToken](scopes.mkString(" ")) match {
      case Some(token) =>
        Future.successful(token)
      case None =>
        wrappedTokenService.acquireToken(credentials, scopes).map { token =>
          cache.set(scopes.mkString(" "), token)
          token
        }

    }
  }

  def invalidateToken: Unit = {
    // this could be implemented in order to invalidate a token, when requested shortly
    // before it's invalid, to prevent unauthorized requests.
  }
}

object OAuthTokenCacheWrapper {
  val DEFAULT_MAX_CACHE_SIZE = 1000
  val DEFAULT_EAGER_EXPIRATION_OFFSET = 5
}
