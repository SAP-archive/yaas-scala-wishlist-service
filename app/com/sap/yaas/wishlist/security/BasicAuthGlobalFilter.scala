package com.sap.yaas.wishlist.security

import java.nio.charset.StandardCharsets
import javax.inject.Inject

import play.api.Configuration
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class BasicAuthGlobalFilter @Inject()(config: Configuration)(implicit context: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    config.getStringSeq("yaas.security.basic_auth_credentials") match {
      case Some(configVals) if (configVals.length > 0) => {
        requestHeader.headers.get("Authorization") match {
          case Some(headerAuth) =>
            if (configVals.exists(cred => {
              val (password, expected) = (cred.getBytes(StandardCharsets.UTF_8), headerAuth)
              val authString = "Basic " + java.util.Base64.getEncoder.encodeToString(password)
              authString == expected
            }))
              nextFilter(requestHeader)
            else Future.successful(Forbidden)
          case None =>
            Future.successful(Forbidden)
        }
      }
      case _ => nextFilter(requestHeader)
    }
  }
}