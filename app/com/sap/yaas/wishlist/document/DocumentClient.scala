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
package com.sap.yaas.wishlist.document

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.sap.yaas.wishlist.model.Wishlist._
import com.sap.yaas.wishlist.model._
import com.sap.yaas.wishlist.util.WSHelper._
import com.sap.yaas.wishlist.util.{WSHelper, YaasLogger}
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{Format, JsSuccess, Json}
import play.api.libs.ws._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/**
 * Document Service client, that handles calls to the document repository
 */
class DocumentClient @Inject()(ws: WSClient, config: Configuration, system: ActorSystem)(implicit context: ExecutionContext) {

  val client: String = config.getString("yaas.client").get
  val logger = YaasLogger(this.getClass)

  val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = config.getInt("yaas.document.max_failures").get,
      callTimeout = Duration(config.getMilliseconds("yaas.document.call_timeout").get, MILLISECONDS),
      resetTimeout = Duration(config.getMilliseconds("yaas.document.reset_timeout").get, MILLISECONDS))
      .onHalfOpen(notifyOnHalfOpen())
      .onOpen(notifyOnOpen())

  /**
   * Event endpoint for circuit breaker half open event
   */
  def notifyOnHalfOpen(): Unit =
    logger.getLogger.warn("CircuitBreaker is now half open, if the next call fails, it will be open again")

  /**
   * Event endpoint for circuit breaker open event
   */
  def notifyOnOpen(): Unit =
    logger.getLogger.warn("CircuitBreaker is now open, and will not close for one minute")

  /**
   * Document service endpoint to get a collection of type wishlist
   * @param token access_token to be used in the request
   * @param pageNumber (optional, default=1) of the page you want to retrieve
   * @param pageSize (optional, default=16) of the pages you want to view
   * @return a Future[Wishlists]
   */
  def getAll(token: String, pageNumber: Option[Int] = None, pageSize: Option[Int] = None)
            (implicit yaasAwareParameters: YaasAwareParameters): Future[Wishlists] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*).withHeaders(
      HeaderNames.AUTHORIZATION -> ("Bearer " + token)).withQueryString(
      "totalCount" -> "true", "pageSize" -> pageSize.getOrElse(0).toString)

    val futureResponse: Future[WSResponse] =
      breaker.withCircuitBreaker(failFast(pageNumber.fold(request)(
        p => request.withQueryString("pageNumber" -> p.toString)).get))

    futureResponse map {
      response =>
        response.status match {
          case OK =>
            val totalCount = response.header("hybris-count").getOrElse("")
            response.json.validate[Wishlists] match {
              case s: JsSuccess[Wishlists] => s.get
              case _ => throw new Exception("Could not parse result: " + response.json)
            }
          case _ => throw new Exception("Unexpected response status: " + response)
        }
    }
  }

  /**
   * Document service endpoint to create an object of type wishlist
   * @param wishlist an object of type wishlist to create in document repository
   * @param token access_token to be used in the request
   * @return a Future[ResourceLocation]
   */
  def create(wishlist: Wishlist, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[ResourceLocation] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.post(Json.toJson(wishlist))))
    futureResponse map {
      response => checkResponse[ResourceLocation](response)
    }
  }

  /**
   * Document service endpoint to get a single object of type wishlist by id
   * @param wishlistId to retrieve
   * @param token access_token to be used in the request
   * @return Future[Wishlist]
   */
  def get(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Wishlist] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.get))
    futureResponse map {
      response => checkResponse[Wishlist](response)
    }
  }

  /**
   * Document service endpoint to update a single object of type wishlist
   * @param wishlist object to be updated
   * @param token access_token to be used in the request
   * @return Future[UpdateResource]
   */
  def update(wishlist: Wishlist, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[UpdateResource] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.put(Json.toJson(wishlist))))
    futureResponse map {
      response =>
        checkResponse[UpdateResource](response)
    }
  }

  /**
   * Helper method to check responses from document service and handle error responses in a central place
   */
  private def checkResponse[A : Format](response: WSResponse): A = {
    response.status match {
      case OK | CREATED =>
        response.json.validate[A] match {
          case s: JsSuccess[A] => s.get
          case _ => throw new Exception("Could not parse result: " + response.json)
        }
      case CONFLICT => throw new DocumentExistsException("Wishlist exists")
      case _ => throw new Exception("Unexpected response status: " + response)
    }
  }

  /**
   * Document service endpoint to delete a single object of type wishlist by id
   * @param wishlistId of the object that should be deleted
   * @param token access_token to be used in the request
   * @return a Future[Unit] (NO CONTENT on success)
   */
  def delete(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Unit] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.delete))
    futureResponse map {
      response =>
        response.status match {
          case NO_CONTENT => ()
          case NOT_FOUND => throw new NotFoundException("resource not found", path)
          case _ => throw new Exception("Unexpected response status: " + response)
        }
    }
  }

}
/**
 * Helper object to provide global vals
 */
object DocumentClient {
  val WISHLIST_PATH: String = "wishlist"
}
