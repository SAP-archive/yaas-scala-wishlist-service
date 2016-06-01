import javax.inject.Inject

import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter
import play.api.http.HttpFilters
import play.filters.cors.CORSFilter

class Filters @Inject() (
  basicAuthFilter: BasicAuthGlobalFilter,
  corsFilter: CORSFilter
) extends HttpFilters {
  val filters = Seq(basicAuthFilter, corsFilter)
}