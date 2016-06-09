package com.sap.cloud.yaas.wishlist.pipeline

import javax.inject.Inject

import com.sap.cloud.yaas.wishlist.context.{YaasAwareParameters, YaasRequest}
import com.sap.cloud.yaas.wishlist.mapper.ErrorMapper
import com.sap.cloud.yaas.wishlist.security.{BasicAuthActionFilter, ManageActionFilter, ViewActionFilter}
import com.sap.cloud.yaas.wishlist.util.YaasLogger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Holds a YaasAction that will extract Yaas headers from the Request, will add them to the result, and will also refine the
  * Request to be a YaasRequest that holds a context.
  */
class YaasActions @Inject()(errorMapper: ErrorMapper, basicAuthActionFilter: BasicAuthActionFilter)(implicit ec: ExecutionContext) {
  val log = YaasLogger(this.getClass)

  val ManageAction = Action andThen RecoverActionBuilder andThen
    YaasAction andThen LogActionBuilder andThen
    basicAuthActionFilter andThen ManageActionFilter

  val ViewAction = Action andThen RecoverActionBuilder andThen YaasAction andThen
    LogActionBuilder andThen basicAuthActionFilter andThen ViewActionFilter

  private[this] object YaasAction extends ActionFunction[Request, YaasRequest] {
    def invokeBlock[A](request: Request[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] =
      Try(
        block(YaasRequest(YaasAwareParameters(request), request)
        ).map(
          _.withHeaders(YaasAwareParameters(request).asSeq: _*))
      ).recover {
        case e: Exception => Future.failed(e)
      }.get
  }

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
      block(request)
    }
  }

  private[this] object RecoverActionBuilder extends ActionFunction[Request, Request] {
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] =
      Try(
        block(request)
      ).recover {
        case e: Exception => Future.failed(e)
      }.get.recover(errorMapper.mapError)
  }
}
