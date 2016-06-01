package com.sap.yaas.wishlist.security

import javax.inject.Inject
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.Configuration
import play.api.mvc.Results._
import java.nio.charset.StandardCharsets
import akka.stream.Materializer

class BasicAuthGlobalFilter @Inject() (config: Configuration)(implicit val mat: Materializer, context: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

   config.getStringSeq("yaas.security.basic_auth_credentials") match {
      case Some(configVals) =>
        if (configVals.length > 0) {
          requestHeader.headers.get("Authorization") match {
            case Some(headerAuth) =>
              if (configVals.length > 0 && configVals.exists(cred =>
                {
                  val (password, expected) = (cred.getBytes(StandardCharsets.UTF_8), headerAuth)
                  val authString = "Basic " + java.util.Base64.getEncoder.encodeToString(password)
                  authString == expected
                }))
                  nextFilter(requestHeader)
                  else Future.successful(Forbidden)
            case None =>
              Future.successful(Forbidden)
          }
        } else
          nextFilter(requestHeader)
      case None =>
        nextFilter(requestHeader)
    }
  }
}