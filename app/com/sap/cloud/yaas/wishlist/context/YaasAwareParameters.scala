package com.sap.cloud.yaas.wishlist.context

import java.util.regex.Pattern

import com.sap.cloud.yaas.servicesdk.patternsupport.traits.YaasAwareTrait.Headers._
import play.api.mvc.Request

import scala.util.Try

/**
  * Encapsulates SAP Hybris required headers to make sure they are properly passed and set.
  */
case class YaasAwareParameters(hybrisTenant: String, hybrisClient: String, hybrisScopes: String,
                               hybrisUser: Option[String], hybrisRequestId: Option[String], hybrisHop: Int) {
  val asSeq: Seq[(String, String)] = Seq(TENANT -> hybrisTenant,
    CLIENT -> hybrisClient,
    HOP -> hybrisHop.toString) ++
    hybrisUser.fold(Seq.empty[(String, String)])(s => Seq(USER -> s)) ++
    hybrisRequestId.fold(Seq.empty[(String, String)])(s => Seq(REQUEST_ID -> s))
}

object YaasAwareParameters {
  private val DEFAULT_HOP = 1

  def apply[A](request: Request[A]): YaasAwareParameters = {
    new YaasAwareParameters(
      extractHeaderWithValidation(request, TENANT, TENANT_PATTERN),
      extractHeaderWithValidation(request, CLIENT, CLIENT_PATTERN),
      request.headers.get(SCOPES).getOrElse(""),
      request.headers.get(USER),
      request.headers.get(REQUEST_ID),
      extractIntHeader(request, HOP).getOrElse(DEFAULT_HOP))
  }

  def extractHeaderWithValidation(request: Request[Any], headerName: String, pattern: Pattern): String = {
    val header = request.headers.get(headerName)
    header.filterNot(_.trim.isEmpty).fold(
      throw new MissingHeaderException(headerName)
    ) { value =>
      if (!pattern.matcher(value).matches()) {
        throw new MalformedHeaderException(headerName)
      }
      value
    }
  }

  def extractIntHeader(request: Request[Any], headerName: String): Option[Int] = {
    Try {
      request.headers.get(headerName).map(header => header.toInt)
    } recover {
      case e: NumberFormatException => throw new MalformedHeaderException(headerName)
    } get
  }
}

/**
  * Definition of an Exception thrown if a required header is missing.
  */
class MissingHeaderException(val headerName: String) extends Exception

/**
  * Definition of an Exception thrown if a required header is invalid.
  */
class MalformedHeaderException(val headerName: String) extends Exception
