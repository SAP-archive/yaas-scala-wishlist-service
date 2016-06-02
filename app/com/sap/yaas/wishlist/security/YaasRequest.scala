package com.sap.yaas.wishlist.security

import com.sap.yaas.wishlist.model.YaasAwareParameters
import play.api.mvc.{Request, WrappedRequest}

case class YaasRequest[A](val yaasContext: YaasAwareParameters, request: Request[A]) extends WrappedRequest[A](request)
