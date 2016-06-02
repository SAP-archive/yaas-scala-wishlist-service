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

import play.api.mvc._

import scala.concurrent.Future

object ManageActionFilter extends ActionFilter[YaasRequest] {

  def filter[A](input: YaasRequest[A]): Future[Option[Result]] = Future.successful {
    val scope = input.headers.get("scope")
    if (!scope.contains(SecurityUtils.MANAGE_SCOPE)) {
      throw new ForbiddenException(scope, Seq(SecurityUtils.MANAGE_SCOPE))
    } else {
      None
    }
  }
}
