package com.sap.yaas.wishlist.security

import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import play.api.cache.CacheApi

object ViewActionFilter extends ActionFilter[YaasRequest] {
  def filter[A](input: YaasRequest[A]) = Future.successful {
    val scope = input.headers.get("scope")
    if (scope.contains(SecurityUtils.VIEW_SCOPE) || scope.contains(SecurityUtils.MANAGE_SCOPE))
      None
    else
      Some(Forbidden)
  }
}