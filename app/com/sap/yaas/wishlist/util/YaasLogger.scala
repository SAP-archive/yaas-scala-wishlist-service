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

import com.sap.yaas.wishlist.model.YaasAwareParameters
import org.slf4j.{Logger, LoggerFactory, MDC}

/**
 * Logging class, overriding necessary methods of the Logger implementation to make the log more YaaS flavored
 */
class YassLogger(underlying: Logger) {

    def getLogger: Logger = underlying

    def getName: String = underlying.getName

    private def format(context: YaasAwareParameters) = context.asSeq + " - " +
      ""

    def isTraceEnabled: Boolean = underlying.isTraceEnabled

    def trace(msg: String)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isTraceEnabled) underlying.trace(format(context) + msg)

    def trace(msg: String, t: Throwable)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isTraceEnabled) underlying.trace(format(context) + msg, t)

    def isDebugEnabled: Boolean = underlying.isDebugEnabled

    def debug(msg: String)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isDebugEnabled) {
          //MDC.set(context als map)
          underlying.debug(format(context) + msg)
          //MDC.clear()
      }

    def debug(msg: String, t: Throwable)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isDebugEnabled) underlying.debug(format(context) + msg, t)

    def isInfoEnabled: Boolean = underlying.isInfoEnabled

    def info(msg: String)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isInfoEnabled) underlying.info(format(context) + msg)

    def info(msg: String, t: Throwable)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isInfoEnabled) underlying.info(format(context) + msg, t)

    def isWarnEnabled: Boolean = underlying.isWarnEnabled

    def warn(msg: String)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isWarnEnabled) underlying.warn(format(context) + msg)

    def warn(msg: String, t: Throwable)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isWarnEnabled) underlying.warn(format(context) + msg, t)

    def isErrorEnabled: Boolean = underlying.isErrorEnabled

    def error(msg: String)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isErrorEnabled) underlying.error(format(context) + msg)

    def error(msg: String, t: Throwable)(implicit context: YaasAwareParameters): Unit =
      if (underlying.isErrorEnabled) underlying.error(format(context) + msg, t)

}

object YaasLogger {

  /**
    * Obtains a logger instance.
    *
    * @param name the name of the logger
    * @return a logger
    */
  def apply(name: String): YassLogger = new YassLogger(LoggerFactory.getLogger(name))

  /**
    * Obtains a logger instance.
    *
    * @param clazz a class whose name will be used as logger name
    * @return a logger
    */
  def apply[T](clazz: Class[T]): YassLogger = new YassLogger(LoggerFactory.getLogger(clazz.getName.stripSuffix("$")))

}
