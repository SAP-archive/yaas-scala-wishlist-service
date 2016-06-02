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
package com.sap.yaas.wishlist.controllers

import play.api.Logger
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class WireLog[A](action: Action[A])(implicit ec: ExecutionContext) extends Action[A] {

  /* All our requests are JSON */
  lazy val parser = action.parser
  val wireLogger: Logger = Logger("yaas.wishlist.wirelog")


  def apply(request: Request[A]): Future[Result] = {
    wireLogger.info("=== REQUEST ===\n")
    wireLogger.info(request.headers.headers.mkString("\n"))
    // TODO log request body!!
    action(request).map(
      result => {
        wireLogger.info("=== RESPONSE ===\n")
        wireLogger.info(result.header.headers.mkString("\n"))
        // TODO log response body!!
        result
      }
    )
  }
}
