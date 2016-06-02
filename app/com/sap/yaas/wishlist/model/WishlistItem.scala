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

/*
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "title": "Wishlist Item",
  "type" : "object",
  "properties":
  {
    "product":
    {
      "type":"string",
      "pattern":"^.+"
    },
    "amount":
    {
      "type":"integer",
      "minimum":1,
      "default":1
    },
    "note":
    {
      "type":"string"
    },
    "createdAt":
    {
      "type":"string",
      "format":"date-time"
    }
  },
  "required":["product", "amount"]
}

 */
case class WishlistItem(product: String,
  amount: Int,
  note: Option[String],
  createdAt: Option[String])

object WishlistItem {
  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._

  implicit val wishlistItemReads: Reads[WishlistItem] = (
    (JsPath \ "product").read[String](minLength[String](1))
    .and((JsPath \ "amount").read[Int](min(1)))
    .and((JsPath \ "note").readNullable[String])
    .and((JsPath \ "createdAt").readNullable[String]))(WishlistItem.apply _)

  implicit val wishlistItemWrites = Json.writes[WishlistItem]

}
