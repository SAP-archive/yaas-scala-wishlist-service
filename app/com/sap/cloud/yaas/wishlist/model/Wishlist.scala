package com.sap.cloud.yaas.wishlist.model

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
        "$ref" : "https://pattern.yaas.io/v1/schema-localized.json"
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
/**
  * Case class for formatting a Wishlist object
  */
case class Wishlist(id: String, owner: String, title: String,
                    items: Seq[WishlistItem], url: Option[String] = None)

object Wishlist {
  /**
    * Definition of Wishlists type as Sequence of multiple or one Wishlist
    */
  type Wishlists = Seq[Wishlist]

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val wishlistReads: Reads[Wishlist] =
    (JsPath \ "id").read[String](minLength[String](1))
      .and((JsPath \ "owner").read[String](minLength[String](1)))
      .and((JsPath \ "title").read[String](minLength[String](1)))
      .and((JsPath \ "items").read[Seq[WishlistItem]])
      .and((JsPath \ "url").readNullable[String])(Wishlist.apply _)

  implicit val wishlistWrites = Json.writes[Wishlist]

}
