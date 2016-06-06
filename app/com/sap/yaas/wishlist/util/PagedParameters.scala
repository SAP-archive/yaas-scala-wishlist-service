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

trait PagedParameters {

  val PAGE_SIZE: String = "pageSize"

  val PAGE_NUMBER: String = "pageNumber"

  val TOTAL_COUNT: String = "totalCount"


}

object PagedParameters extends scala.AnyRef with PagedParameters {
}