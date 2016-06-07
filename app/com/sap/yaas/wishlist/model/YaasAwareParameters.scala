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

import com.sap.cloud.yaas.servicesdk.patternsupport.traits.YaasAwareTrait.Headers._
import com.sap.yaas.wishlist.service.ConstraintViolationException
import play.api.mvc.Request

/**
  * Header propagation helper to make sure the SAP Hybris required headers are properly passed and set
  */
case class YaasAwareParameters(hybrisTenant: String, hybrisClient: String,
                               hybrisScopes: String,
                               hybrisUser: Option[String],
                               hybrisRequestId: Option[String],
                               hybrisHop: Int = 1) {
  val asSeq: Seq[(String, String)] = Seq(TENANT -> hybrisTenant,
    CLIENT -> hybrisClient,
    HOP -> hybrisHop.toString) ++
    (if (hybrisUser.isDefined) Seq(USER -> hybrisUser.get) else Seq()) ++
    (if (hybrisRequestId.isDefined) Seq(REQUEST_ID -> hybrisRequestId.get) else Seq())
}

object YaasAwareParameters {
  def apply[A](request: Request[A]): YaasAwareParameters = {
    new YaasAwareParameters(
      request.headers.get(TENANT).getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get(CLIENT).getOrElse(throw new ConstraintViolationException(Seq.empty[(String, Seq[String])])),
      request.headers.get(SCOPES).getOrElse(""),
      request.headers.get(USER),
      request.headers.get(REQUEST_ID),
      request.headers.get(HOP).getOrElse("1").toInt)
  }


}
