/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2016 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 */

import com.google.inject.Inject
import com.sap.yaas.wishlist.util.ErrorMapper
import play.api.http.HttpErrorHandler
import play.api.mvc.{Result, RequestHeader}
import play.api.mvc.Results._

import scala.concurrent._


/**
  * Default error handler on top of the recover block provided in YaasActions.
  */
class ErrorHandler @Inject()(errorMapper: ErrorMapper) extends HttpErrorHandler {

  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Future.successful(
      Status(statusCode)("A client error occurred: " + message)
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Future.successful(
      errorMapper.mapError(exception)
    )
  }
}