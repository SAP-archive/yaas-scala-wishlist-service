package com.sap.yaas.wishlist.oauth

import com.sap.yaas.wishlist.model.OAuthToken
import javax.inject.Inject
import OAuthTokenCacheWrapper._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.cache.CacheApi

class OAuthTokenCacheWrapper @Inject() (wrappedTokenService: OAuthTokenService,
                                        cache: CacheApi)
                                       (implicit context: ExecutionContext)
                                       extends OAuthTokenProvider {
  
  def acquireToken(clientId: String, clientSecret: String, scope: String): Future[OAuthToken] = {
    cache.get[OAuthToken](scope) match {
      case Some(token) =>
        Future.successful(token)
      case None =>
        wrappedTokenService.acquireToken(clientId, clientSecret, scope).map { token =>
          cache.set(scope, token)
          token
        }
      
    }
  }
  
  def invalidateToken = {
    
  }
}

object OAuthTokenCacheWrapper {
  val DEFAULT_MAX_CACHE_SIZE = 1000
  val DEFAULT_EAGER_EXPIRATION_OFFSET = 5
}