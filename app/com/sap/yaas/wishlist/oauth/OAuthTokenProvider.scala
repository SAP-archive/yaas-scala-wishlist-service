package com.sap.yaas.wishlist.oauth

import scala.concurrent.Future
import com.sap.yaas.wishlist.model.OAuthToken

trait OAuthTokenProvider {
  def acquireToken(clientId: String, clientSecret: String, scopes: Seq[String]): Future[OAuthToken]
  def invalidateToken()
}