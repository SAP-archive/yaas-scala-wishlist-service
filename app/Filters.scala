import javax.inject.Inject
import play.api.http.HttpFilters
import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter


// Do not move this file!! The filter is not used, if the file is outside of this package.
class Filters @Inject() (
  basicAuthFilter: BasicAuthGlobalFilter
) extends HttpFilters {
  val filters = Seq(basicAuthFilter)
}