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
import com.sap.yaas.wishlist.model.{ResourceLocation, Wishlist, WishlistItem}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.Inside._
import org.scalatestplus.play._
import play.api.libs.json.{JsString, Json}
import play.api.test.Helpers._
import play.api.test._

class ApplicationSpec extends PlaySpec with OneAppPerSuite with BeforeAndAfterAll {

  val contentTypeHeader = "Content-Type"
  val contentTypeJson = "application/json"
  val yaasDocumentUrlKey = "yaas.document.url"
  val yaasClientKey = "yaas.client"

  val wiremockPort = 8089

  val tenant = "myTenant"
  val client = "myClient"
  val id = "415-2"
  val link = "http://myLink.com"
  val requestId = "516-3"
  val hop = "4"

  val wishlistItem = new WishlistItem("product", 4, Some("note"), None)
  val wishlist = new Wishlist(id, "owner", "title", List(wishlistItem))
  val wireMockServer: WireMockServer = new WireMockServer(
    WireMockConfiguration.wireMockConfig().port(wiremockPort));

  override def beforeAll() = {
    sys.props ++= Map(yaasDocumentUrlKey -> s"http://localhost:$wiremockPort",
      yaasClientKey -> client)
    wireMockServer.start()
    configureFor("localhost", wiremockPort)
    super.beforeAll()
  }

  override def afterAll() = {
    sys.props -= yaasDocumentUrlKey
    sys.props -= yaasClientKey
    wireMockServer.stop()
    super.afterAll()
  }

  "Application" must {

    "create a wishlist in document service propagating the hybris-requestId" in {
      val path = s"/$tenant/$client/data/wishlist/$id"
      stubFor(post(urlEqualTo(path))
        .withHeader(contentTypeHeader, containing(contentTypeJson))
        .withHeader("hybris-requestId", equalTo(requestId))
        .withHeader("hybris-hop", equalTo(hop))
        .willReturn(
          aResponse().withStatus(CREATED)
            .withHeader(contentTypeHeader, contentTypeJson)
            .withBody(Json.toJson(new ResourceLocation(id, link)).toString())
        )
      )
      val wishlistJson = Json.toJson(wishlist)
      val request = FakeRequest(POST, "/")
        .withHeaders(getDefaultHeaders(): _*)
        .withHeaders(
          "hybris-requestId" -> requestId,
          "hybris-hop" -> hop
        )
        .withBody(wishlistJson)

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe OK
          contentType(result) mustEqual Some(contentTypeJson)
          (contentAsJson(result) \ "id").get mustEqual JsString(id)
          (contentAsJson(result) \ "link").get mustEqual JsString(link)
      }
      WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching(path)).withRequestBody(
        WireMock.equalToJson(wishlistJson.toString())))
    }

    "return a conflict for an already existing wishlist" in {
      stubFor(post(urlEqualTo(s"/$tenant/$client/data/wishlist/$id"))
        .withHeader(contentTypeHeader, containing(contentTypeJson))
        .willReturn(
          aResponse().withStatus(CONFLICT)
        )
      )
      val request = FakeRequest(POST, "/")
        .withHeaders(getDefaultHeaders(): _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe CONFLICT
      }
    }

    "return a 500 for unexpected errors from the document repository" in {
      stubFor(post(urlEqualTo(s"/$tenant/$client/data/wishlist/$id"))
        .withHeader(contentTypeHeader, containing(contentTypeJson))
        .willReturn(
          aResponse().withStatus(INTERNAL_SERVER_ERROR)
        )
      )
      val request = FakeRequest(POST, "/")
        .withHeaders(getDefaultHeaders(): _*)
        .withBody(Json.toJson(wishlist))

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return a 400 for an invalid wishlist json" in {
      val request = FakeRequest(POST, "/")
        .withHeaders(getDefaultHeaders(): _*)
        .withBody("{ \"invalid\" }")

      inside(route(request)) {
        case Some(result) =>
          status(result) mustBe BAD_REQUEST
      }
    }


  }

  private def getDefaultHeaders(): Seq[(String, String)] = Seq(
    contentTypeHeader -> contentTypeJson,
    "hybris-tenant" -> tenant,
    "hybris-client" -> client,
    "scope" -> "altocon.wishlist_manage")

}
