package com.sap.yaas.wishlist.security

import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object ManageActionFilter extends ActionFilter[Request] {
  def filter[A](input: Request[A]) = Future.successful {
    if (!input.headers.get("scope").contains(SecurityUtils.MANAGE_SCOPE))
      Some(Forbidden)
    else
      None
  }
}