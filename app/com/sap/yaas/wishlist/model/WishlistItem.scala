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
/**
  * Case class for a list of WishlistItem objects
  */
case class WishlistItem(product: String,
                        amount: Int,
                        note: Option[String],
                        createdAt: Option[String])

object WishlistItem {

  import play.api.libs.functional.syntax._
  import play.api.libs.json.Reads._
  import play.api.libs.json._

  implicit val wishlistItemReads: Reads[WishlistItem] =
    (JsPath \ "product").read[String](minLength[String](1))
      .and((JsPath \ "amount").read[Int](min(1)))
      .and((JsPath \ "note").readNullable[String])
      .and((JsPath \ "createdAt").readNullable[String])(WishlistItem.apply _)

  implicit val wishlistItemWrites = Json.writes[WishlistItem]

}
