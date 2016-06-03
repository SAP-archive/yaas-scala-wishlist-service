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
package com.sap.yaas.wishlist.util

import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

object WSHelper {

  /**
    * In order for the circuit breaker to detect the failure, we need
    * too deliver a failed future for some http status codes.
    */
  def failFast(wsresponse: Future[WSResponse])(implicit ec: ExecutionContext): Future[WSResponse] =
    wsresponse.map(
      response =>
        response.status match {
          /* fail ws request if we get a 503 */
          case SERVICE_UNAVAILABLE | GATEWAY_TIMEOUT | INSUFFICIENT_STORAGE =>
            throw new Exception(response.statusText)
          case _ => response
        })
}
