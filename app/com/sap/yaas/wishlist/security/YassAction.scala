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
package com.sap.yaas.wishlist.security

import javax.inject.Inject

import com.sap.yaas.wishlist.model.YaasAwareParameters
import com.sap.yaas.wishlist.util.ErrorMapper
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
 * Holds a YaasAction that will extract Yaas header from the Request, will add them to the result, and will also refine the
 * Request to be a YaasRequest that holds a context.
 * ActionBuilder does not allow you to refine the type. ActionTransformer does not allow you put code "around" the invoke.
 * So now we need to get the parameters out of the request twice, which is annoying.
 * TODO Try alternative implementation with Action with YaasActionFunction
 */
class YaasActions @Inject()(errorMapper: ErrorMapper)(implicit ec: ExecutionContext) {

  val YaasAction = YaasActionBuilder andThen YaasActionTransformer
  val ManageAction = YaasAction andThen ManageActionFilter andThen RecoverActionBuilder
  val ViewAction = YaasAction andThen ViewActionFilter andThen RecoverActionBuilder

  private[this] object YaasActionBuilder extends ActionBuilder[Request] {
    // TODO check if yaas required parameters are set? In YaasAwareParameters?
    def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] =
      block(request).map(_.withHeaders(YaasAwareParameters(request).asSeq: _*))
  }

  private[this] object RecoverActionBuilder extends ActionFunction[YaasRequest,YaasRequest] {
    def invokeBlock[A](request: YaasRequest[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] =
      block(request).recover(errorMapper.mapError)
  }

  private[this] object YaasActionTransformer extends ActionTransformer[Request, YaasRequest] {
    def transform[A](request: Request[A]): Future[YaasRequest[A]] =
      Future.successful(YaasRequest(YaasAwareParameters(request), request))
  }

}
