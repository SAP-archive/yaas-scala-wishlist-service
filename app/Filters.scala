import javax.inject.Inject
import play.api.http.HttpFilters
import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter

class Filters @Inject() (
  basicAuthFilter: BasicAuthGlobalFilter
) extends HttpFilters {
  val filters = Seq(basicAuthFilter)
}