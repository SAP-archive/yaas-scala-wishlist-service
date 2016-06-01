package com.sap.yaas.wishlist.security

import play.api.mvc._
import scala.concurrent.Future
import com.sap.yaas.wishlist.model.YaasAwareParameters

class YaasAwareRequest[A](val yaasAwareParameters: YaasAwareParameters, request: Request[A]) extends WrappedRequest[A](request)

object GlobalHeaderPropagateAction extends
    ActionBuilder[YaasAwareRequest] with ActionTransformer[Request, YaasAwareRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val params = YaasAwareParameters.getYaasAwareParameters(request)
    new YaasAwareRequest(params, request)
  }
}