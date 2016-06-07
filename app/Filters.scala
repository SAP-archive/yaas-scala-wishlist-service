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
import javax.inject.Inject

import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter
import com.sap.yaas.wishlist.util.WireLog
import play.Mode
import play.api.Environment
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter

/**
 * Filter class to register global application filters.
 * ATTENTION: Do not move this file!! The filter is not used, if the file is outside of this package.
 */
class Filters @Inject()(
                         basicAuthFilter: BasicAuthGlobalFilter,
                         corsFilter: CORSFilter) extends HttpFilters {
  // exclude WireLog since it cannot be used in production and causes issues with the ApiConsole
  val filters =  Seq(basicAuthFilter, corsFilter)
}
