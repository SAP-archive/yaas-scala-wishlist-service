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

import com.sap.yaas.wishlist.model.Wishlist._
import com.sap.yaas.wishlist.model.{ ResourceLocation, Wishlist, YaasAwareParameters, UpdateResource}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.CircuitBreaker
import akka.actor.ActorSystem
import com.sap.yaas.wishlist.util.YaasLogger

import scala.concurrent.duration._

class DocumentClient @Inject() (ws: WSClient, config: Configuration, system: ActorSystem)(implicit context: ExecutionContext) {

  val client: String = config.getString("yaas.client").get
  val Logger = YaasLogger(this.getClass)

  val breaker =
    new CircuitBreaker(system.scheduler,
      maxFailures = config.getInt("yaas.document.max_failures").getOrElse(5),
      callTimeout = Duration(config.getMilliseconds("yaas.document.call_timeout").getOrElse(10000l),MILLISECONDS),
      resetTimeout = Duration(config.getMilliseconds("yaas.document.reset_timeout").getOrElse(60000l),MILLISECONDS))
        .onHalfOpen(notifyOnHalfOpen())
        .onOpen(notifyOnOpen())

  def notifyOnHalfOpen(): Unit =
    Logger.warn("CircuitBreaker is now half open, if the next call fails, it will be open again")

  def notifyOnOpen(): Unit =
    Logger.warn("CircuitBreaker is now open, and will not close for one minute")

  def getWishlists(token: String, pageNumber: Option[Int] = None, pageSize: Option[Int] = None)(implicit yaasAwareParameters: YaasAwareParameters): Future[Wishlists] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*).withHeaders(
        "Authorization" -> ("Bearer " + token)).withQueryString("totalCount" -> "true", "pageSize" -> pageSize.getOrElse(0).toString)

    val futureResponse: Future[WSResponse] =
      breaker.withCircuitBreaker(failEarly(pageNumber.fold(request)(p => request.withQueryString("pageNumber" -> p.toString())).get))

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

  def create(wishlist: Wishlist, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[ResourceLocation] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders("Authorization" -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failEarly(request.post(Json.toJson(wishlist))))
    futureResponse map { response =>
      response.status match {
        case CREATED => // O
          response.json.validate[ResourceLocation] match {
            case s: JsSuccess[ResourceLocation] => s.get
            case _ => throw new Exception("Could not parse result:" + response.json)
          }
        case CONFLICT => throw new DocumentExistsException("Wishlist exists", path)
        case _ => throw new Exception("Unexpected response status: " + response)

      }
    }
  }

  def getWishlist(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Wishlist] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders("Authorization" -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failEarly(request.get))
    futureResponse map {
      response =>
        response.status match {
          case OK =>
            response.json.validate[Wishlist] match {
              case s: JsSuccess[Wishlist] => s.get
              case _ => throw new Exception("Could not parse result: " + response.json)
            }
          case _ => throw new Exception("Unexpected response status: " + response)
        }
    }
  }

  def update(wishlist: Wishlist, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[UpdateResource] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders("Authorization" -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failEarly(request.put(Json.toJson(wishlist))))
    futureResponse map { response =>
      response.status match {
        case OK => // O
          response.json.validate[UpdateResource] match {
            case s: JsSuccess[UpdateResource] => s.get
            case _ => throw new Exception("Could not parse result:" + response.json)
          }
        case _ => throw new Exception("Unexpected response status: " + response)

      }
    }
  }

  def delete(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Unit] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      client,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders("Authorization" -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failEarly(request.delete))
    futureResponse map {
      response =>
        response.status match {
          case NO_CONTENT =>
            () // empty 
          case _ => throw new Exception("Unexpected response status: " + response)
        }
    }
  }

  def failEarly(wsresponse: Future[WSResponse]): Future[WSResponse] =
    wsresponse.map(
      response =>
        response.status match {
          /* fail ws request if we get a 503 */
          case SERVICE_UNAVAILABLE | GATEWAY_TIMEOUT | INSUFFICIENT_STORAGE => throw new Exception()
          case _ => response
        })

}

object DocumentClient {
  val WISHLIST_PATH: String = "wishlist"
}
