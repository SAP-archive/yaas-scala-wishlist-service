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

import scala.concurrent.Future

trait OAuthTokenProvider {
  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken]

  def invalidateToken(): Unit
}
