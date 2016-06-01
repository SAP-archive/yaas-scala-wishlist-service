import javax.inject.Inject
import play.api.http.HttpFilters
import com.sap.yaas.wishlist.security.BasicAuthGlobalFilter

class Filters @Inject() (
  basicAuthFilter: BasicAuthGlobalFilter
) extends HttpFilters {
  println("hallo")
  val filters = Seq(basicAuthFilter)
}