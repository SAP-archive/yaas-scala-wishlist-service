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

  def update = Action { request =>
    request.body.asJson match {
      case Some(jsvalue) =>
        val jsresult: JsResult[WishlistItem] = jsvalue.validate[WishlistItem]
        jsresult match {
          case _: JsSuccess[WishlistItem] => println("wishlist item: " + jsresult.get)
          case error: JsError => println("Errors: " + JsError.toJson(error).toString())
        }
        Ok("")
      // invalid request body format
      case None =>
        println("Bad Request")
        BadRequest
    }
  }
}
