import javax.inject.Inject

import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter

/**
 * Filter class to register global application filters.
 * ATTENTION: Do not move this file!! The filter is not used, if the file is outside of this package.
 */
class Filters @Inject()(
                         basicAuthFilter: BasicAuthGlobalFilter,
                         corsFilter: CORSFilter) extends HttpFilters {

  val filters =  Seq(basicAuthFilter, corsFilter)
}
