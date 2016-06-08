package com.sap.cloud.yaas.wishlist.security

/**
 * Provides forbidden exception
 */
class ForbiddenException(val scope: Option[String] = None, val requiredScopeIn: Seq[String]) extends Exception

