package com.sap.yaas.wishlist.service

import play.api.data.validation.ValidationError

class ConstraintViolationException(val errors: Seq[(String, Seq[ValidationError])]) extends Exception
