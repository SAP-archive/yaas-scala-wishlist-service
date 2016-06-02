package com.sap.yaas.wishlist.controllers

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.parsing.json.JSON

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