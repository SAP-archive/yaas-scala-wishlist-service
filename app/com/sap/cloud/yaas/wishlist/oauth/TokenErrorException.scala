package com.sap.cloud.yaas.wishlist.oauth

import com.sap.cloud.yaas.wishlist.model.OAuthTokenError

/**
 * Provides token error exception
 */
class TokenErrorException(val tokenError: OAuthTokenError) extends Exception(tokenError.error_description)
