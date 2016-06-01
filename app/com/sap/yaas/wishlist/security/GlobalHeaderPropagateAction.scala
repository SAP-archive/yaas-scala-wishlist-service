package com.sap.yaas.wishlist.security

import play.api.mvc._
import scala.concurrent.Future
import com.sap.yaas.wishlist.model.YaasAwareParameters

class YaasAwareRequest[A](val yaasAwareParameters: YaasAwareParameters, request: Request[A]) extends WrappedRequest[A](request)


object GlobalHeaderPropagateAction extends
    ActionBuilder[YaasAwareRequest] with ActionTransformer[Request, YaasAwareRequest] {
  def transform[A](request: Request[A]) = Future.successful {
    val params = getYaasAwareParameters(request)
    new YaasAwareRequest(params, request)
  }
  
  def getYaasAwareParameters[A](request: Request[A]): YaasAwareParameters = {
    // TODO: validation, currently 500
    new YaasAwareParameters(
      request.headers.get("hybris-tenant").get,
      request.headers.get("hybris-client").get,
      request.headers.get("scope").getOrElse(""),
      request.headers.get("hybris-user"),
      request.headers.get("hybris-requestId"),
      request.headers.get("hybris-hop").getOrElse("1").toInt)
  }
}