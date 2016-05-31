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
package com.sap.yaas.wishlist.document

import javax.inject.Inject

import com.sap.yaas.wishlist.model.{ResourceLocation, Wishlist, YaasAwareParameters}
import play.api.Configuration
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.libs.ws._

import scala.concurrent.{ExecutionContext, Future}

class DocumentClient @Inject()(ws: WSClient, config: Configuration)
                              (implicit context: ExecutionContext) {


  val client: String = config.getString("yaas.client").get

  def create(yaasAwareParameters: YaasAwareParameters,
             wishlist: Wishlist, token: String): Future[ResourceLocation] = {
    val path = List(config.getString("yaas.document.url").get,
      yaasAwareParameters.hybrisTenant,
      "data",
      DocumentClient.WISHLIST_PATH,
      wishlist.id
    ).mkString("/")
    val request: WSRequest = ws.url(path)
      .withHeaders("hybris-tenant" -> yaasAwareParameters.hybrisTenant,
        "hybris-requestId" -> yaasAwareParameters.hybrisRequestId,
        "hybris-hop" -> yaasAwareParameters.hybrisHop.toString,
        "Authorization" -> ("Bearer " + token)
        // ContentType set by Play
      )
    // timeout set by Play: play.ws.timeout.connection
    val futureResponse: Future[WSResponse] = request.post(Json.toJson(wishlist))
    futureResponse map { response =>
      response.status match {
        case 201 => // O
          response.json.validate[ResourceLocation] match {
            case s: JsSuccess[ResourceLocation] => s.get
            case _ => throw new RuntimeException("Could not parse result:" + response.json)
          }
        case 409 => throw new DocumentExistsException(path)
        case _  => throw new RuntimeException("Unexpected response: " + response)

      }
    }
  }
}

object DocumentClient {
  val WISHLIST_PATH: String = "wishlist"
}
