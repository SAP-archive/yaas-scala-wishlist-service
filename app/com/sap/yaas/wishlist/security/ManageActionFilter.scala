package com.sap.yaas.wishlist.security

import com.sap.cloud.yaas.servicesdk.patternsupport.traits.YaasAwareTrait
import play.api.mvc._

import scala.concurrent.Future
/**
 * Enforces necessity of wishlist_manage scope if used in the desired endpoint
 */
object ManageActionFilter extends ActionFilter[YaasRequest] {

  def filter[A](input: YaasRequest[A]): Future[Option[Result]] = Future.successful {
    val scope = input.headers.get(YaasAwareTrait.Headers.SCOPES)
    if (!scope.contains(SecurityUtils.MANAGE_SCOPE)) {
      throw new ForbiddenException(scope, Seq(SecurityUtils.MANAGE_SCOPE))
    } else {
      None
    }
  }
}
