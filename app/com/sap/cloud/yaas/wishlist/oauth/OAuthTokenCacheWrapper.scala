package com.sap.cloud.yaas.wishlist.oauth

import javax.inject.Inject

import com.sap.cloud.yaas.wishlist.model.OAuthToken
import com.sap.cloud.yaas.wishlist.security.Credentials
import play.api.cache.CacheApi

import scala.concurrent.{ExecutionContext, Future}

/**
 * Wrapper, for OAuthTokenService to retrieve and hold tokens in a cache for re-use of an acquired token.
 */
class OAuthTokenCacheWrapper @Inject() (wrappedTokenService: OAuthTokenService,
  cache: CacheApi)(implicit context: ExecutionContext)
    extends OAuthTokenProvider {

  /**
   * Checks the cache if the needed token for this request might be already in cache, if so provide it to
   * the user. If not, request a new one and put it in the cache.
   * @param credentials to be used for the token request
   * @param scopes requested scopes for the request token
   * @return a Future[OAuthToken]
   */
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

  /**
   * UNIMPLEMENTED: Future implementation should cover invalidating tokens. Check in token acquisition if
   * a token is about to expire and then rather request a new one and invalidate the old one, instead of
   * providing the old one as result.
   */
}

object OAuthTokenCacheWrapper {
  val DEFAULT_MAX_CACHE_SIZE = 1000
  val DEFAULT_EAGER_EXPIRATION_OFFSET = 5
}
