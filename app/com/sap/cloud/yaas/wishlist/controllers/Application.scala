package com.sap.cloud.yaas.wishlist.controllers

import com.google.inject.Inject
import com.sap.cloud.yaas.servicesdk.patternsupport.traits.CountableTrait
import com.sap.cloud.yaas.wishlist.controllers.Application._
import com.sap.cloud.yaas.wishlist.document.DocumentClient
import com.sap.cloud.yaas.wishlist.model.Wishlist
import com.sap.cloud.yaas.wishlist.oauth.OAuthTokenCacheWrapper
import com.sap.cloud.yaas.wishlist.security.{Credentials, YaasActions}
import com.sap.cloud.yaas.wishlist.service.ConstraintViolationException
import com.sap.cloud.yaas.wishlist.util.{ErrorMapper, YaasLogger}
import play.api.Configuration
import play.api.data.validation.ValidationError
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsError, JsSuccess, Json, _}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Main entry point, implementing our endpoints defined in the `routes` file
  */
class Application @Inject()(val messagesApi: MessagesApi, documentClient: DocumentClient,
                            oauthClient: OAuthTokenCacheWrapper, errorMapper: ErrorMapper,
                            config: Configuration, yaasActions: YaasActions)
                           (implicit context: ExecutionContext) extends Controller with I18nSupport {

  val logger = YaasLogger(this.getClass)

  val credentials = Credentials(config.getString("yaas.security.client_id").get, config.getString("yaas.security.client_secret").get)

  import yaasActions._

  /**
    * Endpoint implementation for retrieving a list (paginated if desired) of wishlists
    * @param pageNumber (optional, default=1) of the page you want to retrieve
    * @param pageSize (optional, default=16) of the pages you want to view
    * @return a YaasRequest
    */
  def getAll(pageNumber: Option[Int], pageSize: Option[Int]): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_VIEW))
      result <- documentClient.getAll(token.access_token, pageNumber, pageSize).map { response =>
        Results.Ok(Json.toJson(response._1))
          .withHeaders((CountableTrait.ResponseHeaders.COUNT, response._2.getOrElse("")))
      }
    } yield result
  }


  /**
    * Endpoint implementation for creation of a single wishlist
    * @return a YaasRequest
    */
  def create(): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    request.body.validate[Wishlist] match {
      case JsSuccess(jsonWishlist, _) =>
        logger.debug("wishlist: " + jsonWishlist)
        for {
          token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
          result <- documentClient.create(jsonWishlist, token.access_token).map(
            response => Ok(Json.toJson(response)))
        } yield result
      case JsError(errors) =>
        Future.failed(convertToConstraintViolationException(errors))
    }
  }

  /**
    * Endpoint implementation to update a wishlist by id
    * @param wishlistId of the wishlist to update
    * @return a YaasRequest
    */
  def update(wishlistId: String): Action[JsValue] = ManageAction.async(BodyParsers.parse.json) { request =>
    implicit val yaasContext = request.yaasContext
    request.body.validate[Wishlist] match {
      case JsSuccess(jsonWishlist, _) =>
        logger.debug("wishlist item: " + jsonWishlist)
        for {
          token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
          result <- documentClient.update(jsonWishlist, token.access_token).map(
            response => Ok(Json.toJson(response)))
        } yield result
      case JsError(errors) =>
        Future.failed(convertToConstraintViolationException(errors))
    }
  }

  /**
    * Endpoint implementation to delete a wishlist by id
    * @param wishlistId of the wishlist to delete
    * @return a YaasRequest
    */
  def delete(wishlistId: String): Action[AnyContent] = ManageAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_MANAGE))
      result <- documentClient.delete(wishlistId, token.access_token).map(response =>
        NoContent)
    } yield result
  }

  /**
    * Endpoint implementation to get a single wishlist by id
    * @param wishlistId of the wishlist to retrieve
    * @return a YaasRequest
    */
  def get(wishlistId: String): Action[AnyContent] = ViewAction.async { request =>
    implicit val yaasContext = request.yaasContext
    for {
      token <- oauthClient.acquireToken(credentials, Seq(SCOPE_DOCUMENT_VIEW))
      result <- documentClient.get(wishlistId, token.access_token).map(response =>
        Ok(Json.toJson(response)))
    } yield result
  }

  private def convertToConstraintViolationException(errors: Seq[(JsPath,
    Seq[ValidationError])]): ConstraintViolationException = {
    new ConstraintViolationException(errors.map({
      case (path, errlist) => (path.toString, errlist.map(
        error => Messages(error.message)
      ))
    }))
  }

}

/**
  * Helper object to provide global vals
  */
object Application {

  val SCOPE_DOCUMENT_MANAGE = "hybris.document_manage"

  val SCOPE_DOCUMENT_VIEW = "hybris.document_view"

}
