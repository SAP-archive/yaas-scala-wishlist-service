package com.sap.cloud.yaas.wishlist.validation

/**
 * Provides a constraint violation exception encapsulating validation details.
 */
class ConstraintViolationException(val errors: Seq[(String, Seq[String])]) extends Exception
