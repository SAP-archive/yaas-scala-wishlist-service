package com.sap.yaas.wishlist.controllers

import javax.inject.Inject

import com.sap.yaas.wishlist.model.WishlistItem
import play.api.libs.json
import play.api.libs.json.{JsError, JsResult, JsSuccess, Json}
import play.api.mvc._
import com.sap.yaas.wishlist.oauth.OAuthTokenService
import scala.util.{Failure, Success}
import scala.concurrent.{Future, ExecutionContext}
import com.sap.yaas.wishlist.service.RemoteServiceException


/**
  * Created by lutzh on 30.05.16.
  */
class Application @Inject() (oauthClient: OAuthTokenService)(implicit context: ExecutionContext) extends Controller {

  def list = Action.async { request =>
    oauthClient.getToken.map(token =>
      Ok(Json.toJson(WishlistItem.dummyItem) + " + " + token.access_token)).recover({
          case _ =>
            throw new RemoteServiceException("Error during token request, please try again.")
          }
      )
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
