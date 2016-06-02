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
package com.sap.yaas.wishlist.security

import java.nio.charset.StandardCharsets
import javax.inject.Inject
import akka.stream.Materializer
import play.api.Configuration
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

class BasicAuthGlobalFilter @Inject() (config: Configuration)(implicit val mat: Materializer, ec: ExecutionContext) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    config.getStringSeq("yaas.security.basic_auth_credentials") match {
      case Some(configVals) if (configVals.length > 0) => {
        requestHeader.headers.get("Authorization") match {
          case Some(headerAuth) =>
            if (configVals.exists(cred => {
              val (password, expected) = (cred.getBytes(StandardCharsets.UTF_8), headerAuth)
              val authString = "Basic " + java.util.Base64.getEncoder.encodeToString(password)
              authString == expected
            })) {
              nextFilter(requestHeader)
            } else {
              throw new UnauthorizedException
            }
          case None =>
            throw new UnauthorizedException
        }
      }
      case _ => nextFilter(requestHeader)
    }
  }
}
