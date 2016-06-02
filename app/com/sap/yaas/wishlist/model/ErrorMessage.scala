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
  * Provides error information for a request.
  * Defined in https://api.yaas.io/patterns/v1/schema-error-message.json.
  */
case class ErrorMessage(status: Int, `type`: String, message: String, details: Seq[ErrorDetail],
                        moreInfo: URI)

object ErrorMessage {

  import com.sap.yaas.wishlist.util.UriFormat._

  implicit val errorMessageFormat = Json.format[ErrorMessage]

}