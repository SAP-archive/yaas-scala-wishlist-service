package controllers

import model.WishlistItem
import play.api.libs.json
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json}
import play.api.mvc._


/**
  * Created by lutzh on 30.05.16.
  */
class Application extends Controller {

  def list = Action { request =>
    Ok(Json.toJson(WishlistItem.dummyItem))
  }

  def update = Action(BodyParsers.parse.json) { request =>
        val jsresult: JsResult[WishlistItem] = request.body.validate[WishlistItem]
        jsresult match {
          case _: JsSuccess[WishlistItem] =>
            println("wishlist item: " + jsresult.get)
            Ok
          case error: JsError =>
            println("Errors: " + JsError.toJson(error).toString())
            BadRequest
        }
    }
}
