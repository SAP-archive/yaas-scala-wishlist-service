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
package com.sap.yaas.wishlist.util

import java.net.URI

import play.api.libs.json._

object UriFormat {
  implicit val uriReads = Reads {
    js => js match {
      case JsString(s) => JsSuccess(java.net.URI.create(s))
      case _ => JsError("JsString expected to convert to URI")
    }
  }

  implicit val uriWrites = Writes {
    uri: java.net.URI => JsString(uri.toString)
  }
}
