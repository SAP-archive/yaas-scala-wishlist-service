import com.google.inject.Inject
import com.sap.yaas.wishlist.util.ErrorMapper
import play.api.http.HttpErrorHandler
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent._


/**
  * Default error handler on top of the recover block provided in YaasActions.
  */
class ErrorHandler @Inject()(errorMapper: ErrorMapper) extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)(message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      errorMapper.mapError(exception)
    )
  }
}