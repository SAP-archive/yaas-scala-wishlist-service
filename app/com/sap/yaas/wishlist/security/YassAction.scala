package com.sap.yaas.wishlist.security

import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import com.sap.yaas.wishlist.model.YaasAwareParameters

/**
  * Holds a YaasAction that will extract Yaas header from the Request, will add them to the result, and will also refine the
  * Request to be a YaasRequest that holds a context.
  * ActionBuilder does not allow you to refine the type. ActionTransformer does not allow you put code "around" the invoke.
  * So now we need to get the parameters out of the request twice, which is annoying.
  * TODO Try alternative implementation with Action with YaasActionFunction
  */
object YaasActions {

  val YaasAction = YaasActionBuilder andThen YaasActionTransformer

  private[this] object YaasActionBuilder extends ActionBuilder[Request] {
    // TODO check if yaas required parameters are set?
    implicit val ec = executionContext
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]) =
      block(request).map(_.withHeaders(YaasAwareParameters(request).asSeq: _*))
  }

  private[this] object YaasActionTransformer extends  ActionTransformer[Request, YaasRequest] {
    def transform[A](request: Request[A]) = Future.successful(YaasRequest(YaasAwareParameters(request), request))
  }
}
