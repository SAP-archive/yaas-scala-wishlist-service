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

import java.net.URI
import javax.inject._

import com.sap.yaas.wishlist.document.DocumentExistsException
import com.sap.yaas.wishlist.model.{ErrorDetail, ErrorMessage}
import com.sap.yaas.wishlist.security.{ForbiddenException, UnauthorizedException}
import com.sap.yaas.wishlist.service.ConstraintViolationException
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results._
import play.api.mvc._
import play.api.routing.Router

import scala.concurrent._
import com.sap.yaas.wishlist.document.NotFoundException

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
      case e: NotFoundException => NotFound(createBody(e))
      case e: Exception => InternalServerError(createBody(e))
  }

  private def errorTypeForStatus(status: Int): String = {
    statusCodesToErrorTypeMap.getOrElse(status, ErrorMapper.TYPE_INTERNAL_SERVER_ERROR)
  }

  private def createBody(exception: DocumentExistsException): JsValue = {
    createErrorMessage(CONFLICT, "Wishlist already exists")
  }

  private def createBody(exception: UnauthorizedException): JsValue = {
    createErrorMessage(UNAUTHORIZED, "Unauthorized call")
  }

  private def createBody(exception: ConstraintViolationException): JsValue = {
    val details = exception.errors.flatMap(error =>
      error._2.map(errorMessage =>
        createErrorDetail(Some(error._1), "validation_error", errorMessage)
      ))
    createErrorMessage(BAD_REQUEST, "Invalid arguments", details)
  }

  private def createBody(exception: ForbiddenException): JsValue = {
    createErrorMessage(FORBIDDEN, "Missing scope while calling the service. " +
      s"Provided scope: ${exception.scope}, required scope in: ${exception.requiredScopeIn}")
  }
  
  private def createBody(exception: NotFoundException): JsValue = {
    createErrorMessage(NOT_FOUND, "Requested resource is not available")
  }

  private def createBody(exception: Exception): JsValue = {
    Logger.error("Unexpected exception", exception)
    createErrorMessage(INTERNAL_SERVER_ERROR, s"Unexpected error: ${exception.getMessage}")
  }

  private def createErrorMessage(status: Int, message: String,
                                 details: Seq[ErrorDetail] = Nil): JsValue = {
    Json.toJson(ErrorMessage(status, errorTypeForStatus(status), message, details, baseUri))
  }

  private def createErrorDetail(fieldOpt: Option[String], `type`: String, message: String): ErrorDetail = {
    ErrorDetail(fieldOpt, `type`, message, baseUri)
  }
}

object ErrorMapper {
  val TYPE_INTERNAL_SERVER_ERROR = "internal_service_error"
}
