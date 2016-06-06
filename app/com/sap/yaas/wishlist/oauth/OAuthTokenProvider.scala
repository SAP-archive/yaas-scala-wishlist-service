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

import com.sap.yaas.wishlist.model.OAuthToken
import com.sap.yaas.wishlist.security.Credentials

import scala.concurrent.Future

/**
 * Interface for Token acquisition
 */
trait OAuthTokenProvider {
  def acquireToken(credentials: Credentials, scopes: Seq[String]): Future[OAuthToken]

  def invalidateToken(): Unit
}
