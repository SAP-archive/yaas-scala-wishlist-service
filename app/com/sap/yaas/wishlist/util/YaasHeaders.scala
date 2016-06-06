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
package com.sap.yaas.wishlist.util

/**
 * Headers used in the YaasAwareParameter
 */
trait YaasAwareHeaders {

  val HYBRIS_TENANT: String = "hybris-tenant"

  val HYBRIS_CLIENT: String = "hybris-client"

  val HYBRIS_SCOPES: String = "hybris-scopes"

  val HYBRIS_USER: String = "hybris-user"

  val HYBRIS_REQUEST_ID: String = "hybris-request-id"

  val HYBRIS_HOP = "hybris-hop"

}

object YaasAwareHeaders extends scala.AnyRef with YaasAwareHeaders {
}