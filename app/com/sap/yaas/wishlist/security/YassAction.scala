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
  */
class YaasActions @Inject()(errorMapper: ErrorMapper)(implicit ec: ExecutionContext) {

  val ManageAction = Action andThen YaasAction andThen ManageActionFilter andThen RecoverActionBuilder
  val ViewAction = Action andThen YaasAction andThen ViewActionFilter andThen RecoverActionBuilder

  private[this] object RecoverActionBuilder extends ActionFunction[YaasRequest, YaasRequest] {
    def invokeBlock[A](request: YaasRequest[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] =
      block(request).recover(errorMapper.mapError)
  }

  // TODO exception is thrown to early... ;)
  private[this] object YaasAction extends ActionFunction[Request, YaasRequest] {
    def invokeBlock[A](request: Request[A], block: (YaasRequest[A]) => Future[Result]): Future[Result] =
      block(YaasRequest(YaasAwareParameters(request), request)).map(_.withHeaders(YaasAwareParameters(request).asSeq: _*))
  }

}
