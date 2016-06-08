package com.sap.yaas.wishlist.security

import javax.inject.Inject

import com.sap.yaas.wishlist.model.YaasAwareParameters
import com.sap.yaas.wishlist.util.{ErrorMapper, YaasLogger}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Holds a YaasAction that will extract Yaas headers from the Request, will add them to the result, and will also refine the
  * Request to be a YaasRequest that holds a context.
  */
class YaasActions @Inject()(errorMapper: ErrorMapper)(implicit ec: ExecutionContext) {

  val log = YaasLogger(this.getClass)

  val ManageAction = Action andThen YaasAction andThen LogActionBuilder andThen ManageActionFilter andThen RecoverActionBuilder
  val ViewAction = Action andThen YaasAction andThen LogActionBuilder andThen ViewActionFilter andThen RecoverActionBuilder

  private[this] object LogActionBuilder extends ActionFunction[YaasRequest, YaasRequest] {
    def invokeBlock[A](request: YaasRequest[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] = {
      if (log.isInfoEnabled) {
        implicit val yaasContext = request.yaasContext
        val buffer: StringBuilder = new StringBuilder("Server has received a request")
          .append(System.lineSeparator())
        request.headers.headers.foreach(header => buffer.append(header._1)
          .append(": ")
          .append(header._2)
          .append(System.lineSeparator()))
        log.info(buffer.toString())
      }
      block(request).recover(errorMapper.mapError)
    }
  }

  private[this] object RecoverActionBuilder extends ActionFunction[YaasRequest, YaasRequest] {
    def invokeBlock[A](request: YaasRequest[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] =
      block(request).recover(errorMapper.mapError)
  }

  private[this] object YaasAction extends ActionFunction[Request, YaasRequest] {
    def invokeBlock[A](request: Request[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] = {
      Try(YaasAwareParameters(request)) match {
        case Success(yaasParams) => block(YaasRequest(yaasParams, request)).map(_.withHeaders(yaasParams.asSeq: _*))
        case Failure(ex) => Future.failed(ex).recover(errorMapper.mapError)
      }
    }
  }

}
