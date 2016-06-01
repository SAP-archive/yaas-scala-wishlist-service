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

import play.api.mvc.Request

case class YaasAwareParameters(hybrisTenant: String, hybrisClient: String,
                               hybrisScopes: String,
                               hybrisUser: Option[String],
                               hybrisRequestId: Option[String],
                               hybrisHop: Int = 1)

object YaasAwareParameters {

  def getYaasAwareParameters[A](request: Request[A]): YaasAwareParameters = {
    // TODO: validation, currently 500
    new YaasAwareParameters(
      request.headers.get("hybris-tenant").get,
      request.headers.get("hybris-client").get,
      request.headers.get("scope").getOrElse(""),
      request.headers.get("hybris-user"),
      request.headers.get("hybris-requestId"),
      request.headers.get("hybris-hop").getOrElse("1").toInt)
  }
  
}