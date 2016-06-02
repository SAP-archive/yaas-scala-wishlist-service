package com.sap.yaas.wishlist.util

import java.net.URI
import javax.inject._

import com.sap.yaas.wishlist.document.DocumentExistsException
import com.sap.yaas.wishlist.model.{ErrorDetail, ErrorMessage}
import com.sap.yaas.wishlist.security.{ForbiddenException, UnauthorizedException}
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._

class ErrorHandler @Inject()(env: Environment, config: Configuration,
                             sourceMapper: OptionalSourceMapper,
                             router: Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  private val statusCodesToErrorTypeMap = Map(
    400 -> "bad_payload_syntax",
    403 -> "insufficient_permissions",
    404 -> "resource_non_existing",
    401 -> "insufficient_credentials",
    405 -> "unsupported_method",
    406 -> "unsupported_response_content_type",
    409 -> "conflict_resource",
    413 -> "bad_payload_size",
    414 -> "uri_too_long",
    415 -> "unsupported_request_content_type",
    500 -> ErrorHandler.TYPE_INTERNAL_SERVER_ERROR,
    503 -> "service_temporarily_unavailable"
  )

  private val baseUri: URI = {
    new URI(config.getString("yaas.base_url").get)
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    println("===== in onServerError =====")
    Future.successful(exception match {
      case e: DocumentExistsException => Conflict(createBody(e))
      case e: UnauthorizedException => Unauthorized(createBody(e))
      case e: ForbiddenException => Forbidden(createBody(e))
      case e: Exception => InternalServerError(createBody(e))
    })
  }

  override protected def onDevServerError(request: RequestHeader, exception: UsefulException): Future[Result] = {
    println("===== in onDevServerError =====")
    onServerError(request, exception)
  }

  private def errorTypeForStatus(status: Int): String = {
    statusCodesToErrorTypeMap.getOrElse(status, ErrorHandler.TYPE_INTERNAL_SERVER_ERROR)
  }

  private def createBody(exception: DocumentExistsException): JsValue = {
    createErrorMessage(CONFLICT, "Wishlist already exists")
  }

  private def createBody(exception: UnauthorizedException): JsValue = {
    createErrorMessage(UNAUTHORIZED, "Unauthorized to call the service")
  }

  private def createBody(exception: ForbiddenException): JsValue = {
    createErrorMessage(FORBIDDEN, "Missing scope while calling the service. " +
      s"Provided scope: ${exception.scope}, required scope in: ${exception.requiredScopeIn}")
  }

  private def createBody(exception: Exception): JsValue = {
    Logger.error("Unexpected exception", exception)
    createErrorMessage(INTERNAL_SERVER_ERROR, s"Unexpected error: ${exception.getMessage}")
  }

  private def createErrorMessage(status: Int, message: String,
                                 details: List[ErrorDetail] = Nil): JsValue = {

    Json.toJson(ErrorMessage(
      status = status,
      errorTypeForStatus(status),
      message = message,
      moreInfo = baseUri,
      details = details))
  }

  private def createErrorDetail(field: Option[String], `type`: String, message: String): ErrorDetail = {
    ErrorDetail(field, `type` = `type`, message = message, baseUri)
  }
}

object ErrorHandler {
  val TYPE_INTERNAL_SERVER_ERROR = "internal_service_error"
}
