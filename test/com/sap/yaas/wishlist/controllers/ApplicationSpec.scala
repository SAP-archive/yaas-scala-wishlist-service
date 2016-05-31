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
package com.sap.yaas.wishlist.controllers


import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.sap.yaas.wishlist.model.{Wishlist, WishlistItem}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Inside._
import org.scalatestplus.play._
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

  val wishlistItem = new WishlistItem("product", 4, Some("note"), None)
  val wishlist = new Wishlist("myId", "owner", "title", List(wishlistItem))
  val wireMockServer: WireMockServer = new WireMockServer(
    WireMockConfiguration.wireMockConfig().port(8089));

  //No-args constructor will start on port 8080, no HTTPS

  override def beforeAll() = {
    wireMockServer.start()
    super.beforeAll()
  }

  override def afterAll() = {
    wireMockServer.stop()
    super.afterAll()
  }

  "Application" must {

    "create a wishlist in document service " in {
      val request = FakeRequest(POST, "/")
        .withHeaders("Content-Type" -> "application/json",
          "hybris-tenant" -> "myTenant",
          "hybris-client" -> "myClient",
          "scope" -> "wishlist_manage")
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe CREATED
          contentType(result) mustEqual Some("application/json")
          (contentAsJson(result) \ "id").get mustEqual JsString("myId")
          (contentAsJson(result) \ "link").get mustEqual JsString("myLink")
      }
    }
  }

}
