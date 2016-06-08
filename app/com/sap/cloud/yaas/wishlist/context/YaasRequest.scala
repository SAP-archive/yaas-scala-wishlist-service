package com.sap.cloud.yaas.wishlist.context

import play.api.mvc.{Request, WrappedRequest}

/**
  * Wraps a regular request into a yaas aware parameter enriched YaasRequest
  */
case class YaasRequest[A](yaasContext: YaasAwareParameters, request: Request[A])
  extends WrappedRequest[A](request)
