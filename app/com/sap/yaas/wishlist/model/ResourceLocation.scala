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
package com.sap.yaas.wishlist.model

import play.api.libs.json.Json

/**
 * ResourceLocation holding the info of a created document in document service
 */
case class ResourceLocation(id: String, link: String)

object ResourceLocation {
  implicit val ResourceLocationFormat = Json.format[ResourceLocation]

}
