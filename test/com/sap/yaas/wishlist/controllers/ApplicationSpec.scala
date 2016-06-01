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
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.sap.yaas.wishlist.controllers.ApplicationSpec._
import com.sap.yaas.wishlist.model.{ResourceLocation, Wishlist, WishlistItem}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Inside._
import org.scalatestplus.play._
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

  val wishlistItem = new WishlistItem("product", 4, Some("note"), None)
  val wishlist = new Wishlist(TEST_ID, "owner", "title", List(wishlistItem))
  val wireMockServer: WireMockServer = new WireMockServer(
    WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT));

  override def beforeAll() = {
    sys.props ++= Map(YAAS_DOCUMENT_URL -> s"http://localhost:$WIREMOCK_PORT",
      YAAS_CLIENT -> TEST_CLIENT)
    wireMockServer.start()
    configureFor("localhost", WIREMOCK_PORT)
    super.beforeAll()
  }

  override def afterAll() = {
    sys.props -= YAAS_DOCUMENT_URL
    sys.props -= YAAS_CLIENT
    wireMockServer.stop()
    super.afterAll()
  }

  "Application" must {

    "create a wishlist in document service propagating the hybris-requestId" in {
      val path = s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"
      stubFor(post(urlEqualTo(path))
        .withHeader(CONTENT_TYPE_HEADER, containing(CONTENT_TYPE_JSON))
        .withHeader("hybris-requestId", equalTo(TEST_REQUEST_ID))
        .withHeader("hybris-hop", equalTo(TEST_HOP))
        .willReturn(
          aResponse().withStatus(CREATED)
            .withHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .withBody(Json.toJson(new ResourceLocation(TEST_ID, TEST_LINK)).toString())
        )
      )
      val wishlistJson = Json.toJson(wishlist)
      val request = FakeRequest(POST, "/")
        .withHeaders(defaultHeaders: _*)
        .withHeaders(
          "hybris-requestId" -> TEST_REQUEST_ID,
          "hybris-hop" -> TEST_HOP
        )
        .withBody(wishlistJson)

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe OK
          contentType(result) mustEqual Some(CONTENT_TYPE_JSON)
          (contentAsJson(result) \ "id").get mustEqual JsString(TEST_ID)
          (contentAsJson(result) \ "link").get mustEqual JsString(TEST_LINK)
      }
      WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching(path)).withRequestBody(
        WireMock.equalToJson(wishlistJson.toString())))
    }

    "return a conflict for an already existing wishlist" in {
      stubFor(post(urlEqualTo(s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"))
        .withHeader(CONTENT_TYPE_HEADER, containing(CONTENT_TYPE_JSON))
        .willReturn(
          aResponse().withStatus(CONFLICT)
        )
      )
      val request = FakeRequest(POST, "/")
        .withHeaders(defaultHeaders: _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe CONFLICT
      }
    }

    "return a 500 for unexpected errors from the document repository" in {
      stubFor(post(urlEqualTo(s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"))
        .withHeader(CONTENT_TYPE_HEADER, containing(CONTENT_TYPE_JSON))
        .willReturn(
          aResponse().withStatus(INTERNAL_SERVER_ERROR)
        )
      )
      val request = FakeRequest(POST, "/")
        .withHeaders(defaultHeaders: _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a 400 for an invalid wishlist json" in {
      val request = FakeRequest(POST, "/")
        .withHeaders(defaultHeaders: _*)
        .withBody("{ \"invalid\" }")

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe BAD_REQUEST
      }
    }

  }

  val defaultHeaders: Seq[(String, String)] = Seq(
    CONTENT_TYPE_HEADER -> CONTENT_TYPE_JSON,
    "hybris-tenant" -> TEST_TENANT,
    "hybris-client" -> TEST_CLIENT,
    "scope" -> "altocon.wishlist_manage")

}

object ApplicationSpec {

  val CONTENT_TYPE_HEADER = "Content-Type"
  val CONTENT_TYPE_JSON = "application/json"
  val YAAS_DOCUMENT_URL = "yaas.document.url"
  val YAAS_CLIENT = "yaas.client"

  val WIREMOCK_PORT = 8089

  val TEST_TENANT = "myTenant"
  val TEST_CLIENT = "myClient"
  val TEST_ID = "415-2"
  val TEST_LINK = "http://myLink.com"
  val TEST_REQUEST_ID = "516-3"
  val TEST_HOP = "4"

}