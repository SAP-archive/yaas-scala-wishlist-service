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

case class YaasAwareParameters(hybrisTenant: String, hybrisClient: String,
    hybrisScopes: String,
    hybrisUser: Option[String],
    hybrisRequestId: Option[String],
    hybrisHop: Int = 1) {
  val asSeq: Seq[(String, String)] = Seq("hybris-tenant" -> hybrisTenant,
    "hybris-client" -> hybrisClient,
    "hybris-hop" -> hybrisHop.toString) ++
    (if (!hybrisUser.isEmpty) Seq("hybris-ser" -> hybrisUser.get) else Seq()) ++
    (if (!hybrisRequestId.isEmpty) Seq("hybris-request-id" -> hybrisRequestId.get) else Seq())
}

object YaasAwareParameters {
  def apply[A](request: Request[A]): YaasAwareParameters = {
    new YaasAwareParameters(
      request.headers.get("hybris-tenant").getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get("hybris-client").getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get("scope").getOrElse(""),
      request.headers.get("hybris-user"),
      request.headers.get("hybris-request-id"),
      request.headers.get("hybris-hop").getOrElse("1").toInt)
  }
}
