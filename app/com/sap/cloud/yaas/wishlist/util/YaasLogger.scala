package com.sap.cloud.yaas.wishlist.util

import com.sap.cloud.yaas.wishlist.context.YaasAwareParameters
import org.slf4j.{Logger, LoggerFactory, MDC}

/**
  * Logging class, overriding necessary methods of the Logger implementation to make the log more YaaS flavored
  */
class YassLogger(underlying: Logger) {

  def getLogger: Logger = underlying

  def getName: String = underlying.getName

  def isTraceEnabled: Boolean = underlying.isTraceEnabled

  def trace(msg: String)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isTraceEnabled) wrap(msg, (m, t) => underlying.trace(m, t))

  def trace(msg: String, throwable: Throwable)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isTraceEnabled) wrap(msg, (m, t) => underlying.trace(m, t), Some(throwable))

  def isDebugEnabled: Boolean = underlying.isDebugEnabled

  def debug(msg: String)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isDebugEnabled) {
      wrap(msg, (m, t) => underlying.debug(m, t))
    }

  def debug(msg: String, throwable: Throwable)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isDebugEnabled) wrap(msg, (m, t) => underlying.debug(m, t), Some(throwable))

  def isInfoEnabled: Boolean = underlying.isInfoEnabled

  def info(msg: String)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isInfoEnabled) {
      wrap(msg, (m, t) => underlying.info(m, t))
    }

  def info(msg: String, throwable: Throwable)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isInfoEnabled) wrap(msg, (m, t) => underlying.info(m, t), Some(throwable))

  def isWarnEnabled: Boolean = underlying.isWarnEnabled

  def warn(msg: String)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isWarnEnabled) {
      wrap(msg, (m, t) => underlying.warn(m, t))
    }

  def warn(msg: String, throwable: Throwable)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isWarnEnabled) wrap(msg, (m, t) => underlying.warn(m, t), Some(throwable))

  def isErrorEnabled: Boolean = underlying.isErrorEnabled

  def error(msg: String)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isErrorEnabled) {
      wrap(msg, (m, t) => underlying.error(m, t))
    }

  def error(msg: String, throwable: Throwable)(implicit context: YaasAwareParameters): Unit =
    if (underlying.isErrorEnabled) wrap(msg, (m, t) => underlying.error(m, t), Some(throwable))

  private def wrap(msg: String,
                   block: ((String, Throwable) => Unit),
                   throwable: Option[Throwable] = None)
                  (implicit context: YaasAwareParameters): Unit = {
    val headers = context.asSeq
    try {
      setupMDC(headers)
      block(msg, throwable.orNull)
    } finally {
      clearMDC(headers)
    }
  }

  private def setupMDC(headers: Seq[(String, String)]): Unit = {
    headers.foreach(header => MDC.put(header._1.stripPrefix("hybris-")
      .replace("request-id", "requestId"), header._2))
  }

  private def clearMDC(headers: Seq[(String, String)]): Unit = {
    headers.foreach(header => MDC.remove(header._1))
  }

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
