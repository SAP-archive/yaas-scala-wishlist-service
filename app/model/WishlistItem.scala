package model

import play.api.libs.json.Json

case class WishlistItem (product: String,
                         amount: Int,
                         note: Option[String],
                         createdAt: Option[String])

object WishlistItem {
  implicit val WishlistItemFormat = Json.format[WishlistItem]

  val dummyItem = WishlistItem("Fiat 500", 1, Some("Das Auto hätte ich gern"), None)
}
