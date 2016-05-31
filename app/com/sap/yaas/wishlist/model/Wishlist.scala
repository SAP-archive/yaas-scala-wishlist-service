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

case class Wishlist(id: String, owner: String, title: String,
                    items: List[WishlistItem], url: Option[String] = None)

//TODO: Json format for java.net.URI

object Wishlist {
  implicit val WishlistFormat = Json.format[Wishlist]
}