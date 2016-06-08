package com.sap.yaas.wishlist.oauth

import com.sap.yaas.wishlist.model.OAuthTokenError

/**
 * Provides token error exception
 */
class TokenErrorException(val tokenError: OAuthTokenError) extends Exception(tokenError.error_description)
