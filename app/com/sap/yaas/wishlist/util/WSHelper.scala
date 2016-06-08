package com.sap.yaas.wishlist.util

import play.api.http.Status._
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

/**
 * Helper object for Circuit Breaker to fail fast for certain error codes
 */
object WSHelper {

  /**
    * In order for the circuit breaker to detect the failure, we need
    * too deliver a failed future for some http status codes.
    */
  def failFast(wsresponse: Future[WSResponse])(implicit ec: ExecutionContext): Future[WSResponse] =
    wsresponse.map(
      response =>
        response.status match {
          /* fail ws request if we get any of the below status codes */
          case SERVICE_UNAVAILABLE | GATEWAY_TIMEOUT | INSUFFICIENT_STORAGE =>
            throw new Exception(response.statusText)
          case _ => response
        })
}
