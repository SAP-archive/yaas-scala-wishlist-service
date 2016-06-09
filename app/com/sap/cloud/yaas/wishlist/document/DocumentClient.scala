package com.sap.cloud.yaas.wishlist.document

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.pattern.CircuitBreaker
import com.sap.cloud.yaas.servicesdk.patternsupport.traits.{CountableTrait, PagedTrait}
import com.sap.cloud.yaas.wishlist.context.YaasAwareParameters
import com.sap.cloud.yaas.wishlist.document.DocumentClient._
import com.sap.cloud.yaas.wishlist.model.Wishlist._
import com.sap.cloud.yaas.wishlist.model._
import com.sap.cloud.yaas.wishlist.util.WSHelper._
import com.sap.cloud.yaas.wishlist.util.YaasLogger
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
            (implicit yaasAwareParameters: YaasAwareParameters): Future[(Wishlists, Option[String])] = {
    val path = List(config.getString(YAAS_DOCUMENT_URL).get,
      yaasAwareParameters.hybrisTenant,
      client,
      DATA_PATH,
      DocumentClient.WISHLIST_PATH).mkString(PATH_SEPARATOR)
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*).withHeaders(
      HeaderNames.AUTHORIZATION -> ("Bearer " + token)).withQueryString(
      CountableTrait.QueryParameters.TOTAL_COUNT -> "true",
      PagedTrait.QueryParameters.PAGE_SIZE -> pageSize.getOrElse(0).toString)

    val futureResponse: Future[WSResponse] =
      breaker.withCircuitBreaker(failFast(pageNumber.fold(request)(
        p => request.withQueryString(PagedTrait.QueryParameters.PAGE_NUMBER -> p.toString)).get))

    futureResponse map {
      response => (checkResponse[Wishlists](response).get,
        response.header(CountableTrait.ResponseHeaders.COUNT))
    }
  }

  /**
    * Document service endpoint to create an object of type wishlist
    * @param wishlist an object of type wishlist to create in document repository
    * @param token access_token to be used in the request
    * @return a Future[ResourceLocation]
    */
  def create(wishlist: Wishlist, token: String)
            (implicit yaasAwareParameters: YaasAwareParameters): Future[ResourceLocation] = {
    val path = List(config.getString(YAAS_DOCUMENT_URL).get,
      yaasAwareParameters.hybrisTenant,
      client,
      DATA_PATH,
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString(PATH_SEPARATOR)
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(
      failFast(request.post(Json.toJson(wishlist))))
    futureResponse map {
      response => checkResponse[ResourceLocation](response).get
    }
  }

  /**
    * Document service endpoint to get a single object of type wishlist by id
    * @param wishlistId to retrieve
    * @param token access_token to be used in the request
    * @return Future[Wishlist]
    */
  def get(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Wishlist] = {
    val path = List(config.getString(YAAS_DOCUMENT_URL).get,
      yaasAwareParameters.hybrisTenant,
      client,
      DATA_PATH,
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString(PATH_SEPARATOR)
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.get))
    futureResponse map {
      response => checkResponse[Wishlist](response).get
    }
  }

  /**
    * Document service endpoint to update a single object of type wishlist
    * @param wishlist object to be updated
    * @param token access_token to be used in the request
    * @return Future[UpdateResource]
    */
  def update(wishlist: Wishlist, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[UpdateResult] = {
    val path = List(config.getString(YAAS_DOCUMENT_URL).get,
      yaasAwareParameters.hybrisTenant,
      client,
      DATA_PATH,
      DocumentClient.WISHLIST_PATH,
      wishlist.id).mkString(PATH_SEPARATOR)
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.put(Json.toJson(wishlist))))
    futureResponse map {
      response =>
        checkResponse[UpdateResult](response).get
    }
  }

  /**
    * Document service endpoint to delete a single object of type wishlist by id
    * @param wishlistId of the object that should be deleted
    * @param token access_token to be used in the request
    * @return a Future[Unit] (NO CONTENT on success)
    */
  def delete(wishlistId: String, token: String)(implicit yaasAwareParameters: YaasAwareParameters): Future[Unit] = {
    val path = List(config.getString(YAAS_DOCUMENT_URL).get,
      yaasAwareParameters.hybrisTenant,
      client,
      DATA_PATH,
      DocumentClient.WISHLIST_PATH,
      wishlistId).mkString(PATH_SEPARATOR)
    val request: WSRequest = ws.url(path)
      .withHeaders(yaasAwareParameters.asSeq: _*)
      .withHeaders(HeaderNames.AUTHORIZATION -> ("Bearer " + token))
    val futureResponse: Future[WSResponse] = breaker.withCircuitBreaker(failFast(request.delete))
    futureResponse map {
      response => checkResponse(response)
    }
  }

  /**
    * Helper method to check responses from document service and handle error responses in a central place
    */
  private def checkResponse[A: Format](response: WSResponse): Option[A] = {
    response.status match {
      case OK | CREATED =>
        response.json.validate[A] match {
          case s: JsSuccess[A] => Some(s.get)
          case _ => throw new Exception("Could not parse result: " + response.json)
        }
      case NOT_FOUND => throw new DocumentNotFoundException("resource not found")
      case CONFLICT => throw new DocumentExistsException("Wishlist exists")
      case NO_CONTENT => None
      case _ => throw new Exception("Unexpected response status: " + response)
    }
  }

  private def getAuthorizationHeader(token: String): String = s"$BEARER $token"

}

/**
  * Helper object to provide global vals
  */
object DocumentClient {
  private val WISHLIST_PATH: String = "wishlist"

  private val DATA_PATH: String = "data"

  private val YAAS_DOCUMENT_URL: String = "yaas.document.url"

  private val PATH_SEPARATOR: String = "/"

  private val BEARER = "Bearer"
}
