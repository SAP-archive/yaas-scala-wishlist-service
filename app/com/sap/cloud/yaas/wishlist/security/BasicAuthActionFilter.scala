package com.sap.cloud.yaas.wishlist.security

import java.nio.charset.StandardCharsets

import com.google.inject.Inject
import com.sap.cloud.yaas.wishlist.com.sap.cloud.yaas.wishlist.config.Config
import com.sap.cloud.yaas.wishlist.context.YaasRequest
import play.api.http.HeaderNames
import play.api.mvc._

import scala.concurrent.Future

/**
  * Adds support for basic authentication.
  */
class BasicAuthActionFilter @Inject()(config: Config) extends ActionFilter[YaasRequest] {

  def filter[A](input: YaasRequest[A]): Future[Option[Result]] = Future.successful {
    config.basicAuthCredentials match {
      case Some(configVals) if configVals.nonEmpty && input.path.startsWith("/wishlist") =>
        input.headers.get(HeaderNames.AUTHORIZATION) match {
          case Some(headerAuth) =>
            if (configVals.exists(cred => {
              val (password, expected) = (cred.getBytes(StandardCharsets.UTF_8), headerAuth)
              val authString = "Basic " + java.util.Base64.getEncoder.encodeToString(password)
              authString == expected
            })) {
              None
            } else {
              throw new UnauthorizedException
            }
          case None =>
            throw new UnauthorizedException
        }
      case _ => None
    }
  }
}
