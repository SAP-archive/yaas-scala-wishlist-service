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

import java.net.URI

import play.api.libs.json.Json

/**
  * Provides error detail information for a specific field.
  * Defined in https://api.yaas.io/patterns/v1/schema-error-message.json.
  */
case class ErrorDetail(field: Option[String] = None, `type`: String, message: String,
                       moreInfo: URI)

object ErrorDetail {

  import com.sap.yaas.wishlist.util.UriFormat._

  implicit val errorDetailFormat = Json.format[ErrorDetail]

}
