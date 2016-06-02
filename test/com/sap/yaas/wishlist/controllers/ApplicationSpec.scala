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
import com.sap.yaas.wishlist.model.{OAuthToken, ResourceLocation, Wishlist, WishlistItem}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Inside._
import org.scalatestplus.play._
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

  val wishlistItem = new WishlistItem("product", 4, Some("note"), None)
  val wishlistInvalidItem = new WishlistItem("", 0, Some("note"), None)
  val wishlist = new Wishlist(TEST_ID, "owner", "title", List(wishlistItem))
  val invalidWishlist = new Wishlist(TEST_ID, "owner", "title", List(wishlistItem, wishlistInvalidItem))

  val wireMockServer: WireMockServer = new WireMockServer(
    WireMockConfiguration.wireMockConfig().port(WIREMOCK_PORT));

  override def beforeAll(): Unit = {
    val wiremockUrl = s"http://localhost:$WIREMOCK_PORT"
    sys.props ++= Map(YAAS_DOCUMENT_URL -> wiremockUrl,
      YAAS_SECURITY_OAUTH_URL -> wiremockUrl,
      YAAS_CLIENT -> TEST_CLIENT
    )
    wireMockServer.start()
    configureFor("localhost", WIREMOCK_PORT)
    def stubOauthService(): Unit = {
      stubFor(post(urlEqualTo("/token"))
        .willReturn(
          aResponse().withStatus(OK).withBody(Json.toJson(
            new OAuthToken("", TEST_TOKEN, TEST_TOKEN_EXPIRY, "")).toString)
        ))
    }
    stubOauthService()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    sys.props -= YAAS_DOCUMENT_URL
    sys.props -= YAAS_SECURITY_OAUTH_URL
    sys.props -= YAAS_CLIENT
    wireMockServer.stop()
    super.afterAll()
  }

  "Application" must {

    "create a wishlist in document service propagating the hybris-requestId" in {
      val path = s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"

      stubFor(post(urlEqualTo(path))
        .withHeader(CONTENT_TYPE_HEADER, containing(JSON))
        .withHeader("hybris-requestId", equalTo(TEST_REQUEST_ID))
        .withHeader("hybris-hop", equalTo(TEST_HOP))
        .withHeader("Authorization", containing(TEST_TOKEN))
        .willReturn(
          aResponse().withStatus(CREATED)
            .withHeader(CONTENT_TYPE_HEADER, JSON)
            .withBody(Json.toJson(new ResourceLocation(TEST_ID, TEST_LINK)).toString())
        )
      )
      val wishlistJson = Json.toJson(wishlist)
      val request = FakeRequest(POST, WISHLIST_PATH)
        .withHeaders(defaultHeaders: _*)
        .withHeaders(
          "hybris-requestId" -> TEST_REQUEST_ID,
          "hybris-hop" -> TEST_HOP
        )
        .withBody(wishlistJson)

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe OK
          contentType(result) mustEqual Some(JSON)
          (contentAsJson(result) \ "id").get mustEqual JsString(TEST_ID)
          (contentAsJson(result) \ "link").get mustEqual JsString(TEST_LINK)
      }
      WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching(path)).withRequestBody(
        WireMock.equalToJson(wishlistJson.toString())))
    }

    "return a conflict for an already existing wishlist" in {
      stubFor(post(urlEqualTo(s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"))
        .withHeader(CONTENT_TYPE_HEADER, containing(JSON))
        .willReturn(
          aResponse().withStatus(CONFLICT)
        )
      )
      val request = FakeRequest(POST, WISHLIST_PATH)
        .withHeaders(defaultHeaders: _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe CONFLICT
        case _ => println("error")
      }
    }

    "return a 400 for an invalid wishlist" in {
      val request = FakeRequest(POST, WISHLIST_PATH)
        .withHeaders(defaultHeaders: _*)
        .withBody(Json.toJson(invalidWishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "return a 400 for an invalid wishlist json" in {
      val request = FakeRequest(POST, WISHLIST_PATH)
        .withHeaders(defaultHeaders: _*)
        .withBody("{ \"invalid\" }")

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe BAD_REQUEST
      }
    }

    "return a 500 for unexpected errors from the document repository" in {
      stubFor(post(urlEqualTo(s"/$TEST_TENANT/$TEST_CLIENT/data/wishlist/$TEST_ID"))
        .withHeader(CONTENT_TYPE_HEADER, containing(JSON))
        .willReturn(
          aResponse().withStatus(INTERNAL_SERVER_ERROR)
        )
      )
      val request = FakeRequest(POST, WISHLIST_PATH)
        .withHeaders(defaultHeaders: _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

  }

  val defaultHeaders: Seq[(String, String)] = Seq(
    CONTENT_TYPE_HEADER -> JSON,
    "hybris-tenant" -> TEST_TENANT,
    "hybris-client" -> TEST_CLIENT,
    "scope" -> "altocon.wishlist_manage")

}

object ApplicationSpec {

  val CONTENT_TYPE_HEADER = "Content-Type"
  val YAAS_DOCUMENT_URL = "yaas.document.url"
  val YAAS_SECURITY_OAUTH_URL = "yaas.security.oauth_url"
  val YAAS_CLIENT = "yaas.client"
  val WISHLIST_PATH = "/wishlists"


  val WIREMOCK_PORT = 8089

  val TEST_TENANT = "myTenant"
  val TEST_CLIENT = "myClient"
  val TEST_ID = "415-2"
  val TEST_LINK = "http://myLink.com"
  val TEST_REQUEST_ID = "516-3"
  val TEST_HOP = "4"
  val TEST_TOKEN = "token"
  val TEST_TOKEN_EXPIRY = 3600

}