package com.sap.yaas.wishlist.security

import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future

object ViewActionFilter extends ActionFilter[Request] {
  def filter[A](input: Request[A]) = Future.successful {
    if (!input.headers.get("scope").contains(SecurityUtils.VIEW_SCOPE))
      Some(Forbidden)
    else
      None
  }
}