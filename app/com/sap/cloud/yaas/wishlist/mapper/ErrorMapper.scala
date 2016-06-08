package com.sap.cloud.yaas.wishlist.mapper

import java.net.URI
import javax.inject._

import com.sap.cloud.yaas.wishlist.context.{MalformedHeaderException, MissingHeaderException}
import com.sap.cloud.yaas.wishlist.document.{DocumentExistsException, DocumentNotFoundException}
import com.sap.cloud.yaas.wishlist.model.{ErrorDetail, ErrorMessage}
import com.sap.cloud.yaas.wishlist.security.{ForbiddenException, UnauthorizedException}
import com.sap.cloud.yaas.wishlist.validation.ConstraintViolationException
import play.api._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc._

/**
  * Maps common status codes to exceptions.
  */
class ErrorMapper @Inject()(config: Configuration) {

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
    500 -> ErrorMapper.TYPE_INTERNAL_SERVER_ERROR,
    503 -> "service_temporarily_unavailable"
  )

  private val baseUri: URI = {
    new URI(config.getString("yaas.base_url").get)
  }

  val mapError: PartialFunction[Throwable, Result] = {
    case e: DocumentExistsException => Conflict(createBody(e))
    case e: UnauthorizedException => Unauthorized(createBody(e))
    case e: ForbiddenException => Forbidden(createBody(e))
    case e: ConstraintViolationException => BadRequest(createBody(e))
    case e: MissingHeaderException => BadRequest(createBody(e))
    case e: MalformedHeaderException => BadRequest(createBody(e))
    case e: DocumentNotFoundException => NotFound(createBody(e))

    case e: Exception => InternalServerError(createBody(e))
  }

  /**
    * Maps a status code to an exception if handled by the mapper, otherwise maps it to internal server error.
    */
  private def errorTypeForStatus(status: Int): String = {
    statusCodesToErrorTypeMap.getOrElse(status, ErrorMapper.TYPE_INTERNAL_SERVER_ERROR)
  }

  /**
    * Creates a message body for a document exists exception
    */
  private def createBody(exception: DocumentExistsException): JsValue = {
    createErrorMessage(CONFLICT, "Wishlist already exists")
  }

  /**
    * Creates a message body for an unauthorized exception
    */
  private def createBody(exception: UnauthorizedException): JsValue = {
    createErrorMessage(UNAUTHORIZED, "The resource requires authentication.")
  }

  /**
    * Creates a message body for a constraint violation exception
    */
  private def createBody(exception: ConstraintViolationException): JsValue = {
    val details = exception.errors.flatMap(error =>
      error._2.map(errorMessage =>
        createErrorDetail(Some(error._1), "validation_error", errorMessage)
      ))
    createErrorMessage(BAD_REQUEST, "Invalid arguments, see details for more info", "validation_violation", details)
  }

  /**
    * Creates a message body for a forbidden exception
    */
  private def createBody(exception: ForbiddenException): JsValue = {
    createErrorMessage(FORBIDDEN, "The client is not authorized to access this resource. " +
      s"Provided scope: ${exception.scope.getOrElse("")}, required scope any of: ${exception.requiredScopeIn}")
  }

  /**
    * Creates a message body for a not found exception
    */
  private def createBody(exception: DocumentNotFoundException): JsValue = {
    createErrorMessage(NOT_FOUND, "The requested resource is not available.")
  }

  /**
    * Creates a message body for a malformed header exception
    */
  private def createBody(exception: MalformedHeaderException): JsValue = {
    createErrorMessage(BAD_REQUEST,
      s"One or more headers sent in the request have invalid format: ${exception.headerName}",
      "invalid_header", Nil)
  }

  /**
    * Creates a message body for a missing header exception
    */
  private def createBody(exception: MissingHeaderException): JsValue = {
    createErrorMessage(BAD_REQUEST,
      s"Header '${exception.headerName}' is required but was not provided in the request.",
      "missing_required_header", Nil)
  }

  /**
    * Creates a message body for a general exception
    */
  private def createBody(exception: Exception): JsValue = {
    Logger.error("Unexpected exception", exception)
    createErrorMessage(INTERNAL_SERVER_ERROR, s"Unexpected error: ${exception.getMessage}")
  }

  /**
    * Creates a json response object to be returned to the api user
    */
  private def createErrorMessage(status: Int, message: String,
                                 details: Seq[ErrorDetail] = Nil): JsValue = {
    createErrorMessage(status, message, errorTypeForStatus(status), details)
  }

  /**
    * Creates a json response object to be returned to the api user
    */
  private def createErrorMessage(status: Int, message: String, errorType: String,
                                 details: Seq[ErrorDetail]): JsValue = {
    Json.toJson(ErrorMessage(status, errorType, message, details, baseUri))
  }

  /**
    * Creates error details to be added to the error message
    */
  private def createErrorDetail(fieldOpt: Option[String], errorType: String, message: String): ErrorDetail = {
    ErrorDetail(fieldOpt, errorType, message, baseUri)
  }
}

object ErrorMapper {

  private val TYPE_INTERNAL_SERVER_ERROR = "internal_service_error"

  private val TYPE_NOT_FOUND_ERROR = "not_found_error"
}
