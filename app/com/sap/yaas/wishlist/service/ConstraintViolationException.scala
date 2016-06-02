package com.sap.yaas.wishlist.service

import play.api.data.validation.ValidationError

class ConstraintViolationException(val errors: Seq[(String, Seq[String])]) extends Exception
