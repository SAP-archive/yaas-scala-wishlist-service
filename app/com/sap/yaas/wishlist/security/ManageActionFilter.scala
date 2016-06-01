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

import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object ManageActionFilter extends ActionFilter[Request] {

  def filter[A](input: Request[A]): Future[Option[Result]] = Future.successful {
    if (!input.headers.get("scope").contains(SecurityUtils.MANAGE_SCOPE)) {
      Some(Forbidden)
    } else {
      None
    }
  }
}
