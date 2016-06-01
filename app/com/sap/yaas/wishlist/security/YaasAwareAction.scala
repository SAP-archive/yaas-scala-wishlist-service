package com.sap.yaas.wishlist.security

import play.api.mvc.Action
import scala.concurrent.Future
import play.api.mvc.Result
import play.api.mvc.Request
import com.sap.yaas.wishlist.model.YaasAwareParameters
import scala.concurrent.ExecutionContext

case class YaasAware[A](action: Action[A])(implicit ec: ExecutionContext) extends Action[A] {
  
  def apply(request: Request[A]): Future[Result] = {

    val yaasAwareParameters = YaasAwareParameters.getYaasAwareParameters(request)
    val yaasAwareRequest = new YaasAwareRequest(yaasAwareParameters, request)
    action.apply(yaasAwareRequest).map(identity)
  }
  
  
  lazy val parser = action.parser
}