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

import play.api.libs.json.Json

case class WishlistItem(product: String,
                        amount: Int,
                        note: Option[String],
                        createdAt: Option[String])

object WishlistItem {
  implicit val WishlistItemFormat = Json.format[WishlistItem]

  val dummyItem = WishlistItem("Fiat 500", 1, Some("Das Auto hätte ich gern"), None)
}
