package com.sap.cloud.yaas.wishlist.security

import java.nio.charset.StandardCharsets
import javax.inject.Inject

import akka.stream.Materializer
import com.sap.cloud.yaas.wishlist.mapper.ErrorMapper
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Global filter, that enforces use of Basic Auth if credentials are configured via env variable or application.conf
  */
class BasicAuthGlobalFilter @Inject()(config: Configuration, errorMapper: ErrorMapper)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    config.getStringSeq("yaas.security.basic_auth_credentials") match {
      case Some(configVals) if configVals.nonEmpty && requestHeader.path.startsWith("/wishlist") =>
        requestHeader.headers.get(HeaderNames.AUTHORIZATION) match {
          case Some(headerAuth) =>
            if (configVals.exists(cred => {
              val (password, expected) = (cred.getBytes(StandardCharsets.UTF_8), headerAuth)
              val authString = "Basic " + java.util.Base64.getEncoder.encodeToString(password)
              authString == expected
            })) {
              nextFilter(requestHeader)
            } else {
              Future.successful(errorMapper.mapError(new UnauthorizedException))
            }
          case None =>
            Future.successful(errorMapper.mapError(new UnauthorizedException))
        }
      case _ => nextFilter(requestHeader)
    }
  }
}
