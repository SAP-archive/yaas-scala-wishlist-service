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
  "title":"Wishlist",
  "type":"object",
  "properties":
  {
    "id":
    {
      "type":"string"
    },
    "url":
    {
      "type":"string",
      "format":"uri"
    },
    "owner":
    {
      "type":"string",
      "pattern":"^.+"
    },
    "title":
    {
      "type":"string",
      "pattern":"^.+"
    },
    "description":
    {
        "$ref" : "https://api.yaas.io/patterns/v1/schema-localized.json"
    },
    "createdAt":
    {
      "type":"string",
      "format":"date-time"
    },
    "items":
    {
      "type":"array",
      "items":
      {
        "$ref":"wishlistItem"
      }
    }
  },
  "required":["id","owner","title"]
}

 */
case class Wishlist(id: String, owner: String, title: String,
                    items: Seq[WishlistItem], url: Option[String] = None)

//TODO: Json format for java.net.URI

object Wishlist {
  type Wishlists = Seq[Wishlist]

  import play.api.libs.json._
  import play.api.libs.json.Reads._
  import play.api.libs.functional.syntax._


  implicit val wishlistReads: Reads[Wishlist] = (
    (JsPath \ "name").read[String](minLength[String](1))
      .and((JsPath \ "owner").read[String](minLength[String](1)))
      .and((JsPath \ "title").read[String](minLength[String](1)))
      .and((JsPath \ "items").read[Seq[WishlistItem]])
      .and((JsPath \ "url").readNullable[String])
    )(Wishlist.apply _)

  implicit val wishlistWrites = Json.writes[Wishlist]

}
