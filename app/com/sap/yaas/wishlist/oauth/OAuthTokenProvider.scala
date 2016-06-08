package com.sap.yaas.wishlist.oauth

import com.sap.yaas.wishlist.model.OAuthToken
import com.sap.yaas.wishlist.security.Credentials

import scala.concurrent.Future

/**
 * Interface for Token acquisition
 */
trait OAuthTokenProvider {

  /**
    * Queries the OAuth2 service requesting a new token with the given scopes and credentials.
    * @param credentials to be used for the token request
    * @param scopes requested scopes for the request token
    * @return a Future[OAuthToken]
    */
  def acquireToken(credentials: Credentials, scopes: Seq[String]): Future[OAuthToken]

}
