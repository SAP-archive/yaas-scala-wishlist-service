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
package com.sap.yaas.wishlist.model

import com.sap.yaas.wishlist.service.ConstraintViolationException
import play.api.mvc.Request
import com.sap.yaas.wishlist.util.YaasAwareHeaders._

case class YaasAwareParameters(hybrisTenant: String, hybrisClient: String,
    hybrisScopes: String,
    hybrisUser: Option[String],
    hybrisRequestId: Option[String],
    hybrisHop: Int = 1) {
  val asSeq: Seq[(String, String)] = Seq(HYBRIS_TENANT -> hybrisTenant,
    HYBRIS_CLIENT -> hybrisClient,
    HYBRIS_HOP -> hybrisHop.toString) ++
    (if (!hybrisUser.isEmpty) Seq(HYBRIS_USER -> hybrisUser.get) else Seq()) ++
    (if (!hybrisRequestId.isEmpty) Seq(HYBRIS_REQUEST_ID -> hybrisRequestId.get) else Seq())
}

object YaasAwareParameters {
  def apply[A](request: Request[A]): YaasAwareParameters = {
    new YaasAwareParameters(
      request.headers.get(HYBRIS_TENANT).getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get(HYBRIS_CLIENT).getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get(HYBRIS_SCOPES).getOrElse(""),
      request.headers.get(HYBRIS_USER),
      request.headers.get(HYBRIS_REQUEST_ID),
      request.headers.get(HYBRIS_HOP).getOrElse("1").toInt)
  }
}
