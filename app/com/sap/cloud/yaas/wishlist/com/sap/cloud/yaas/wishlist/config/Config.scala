package com.sap.cloud.yaas.wishlist.config

import javax.inject.Inject

import com.sap.cloud.yaas.wishlist.security.Credentials
import play.api.Configuration

import scala.concurrent.duration._

class Config @Inject()(config: Configuration) {

  val client: String = getConfiguration("yaas.client")

  val credentials = Credentials(
    getConfiguration("yaas.security.client_id"),
    getConfiguration("yaas.security.client_secret"))

  val oauthUrl: String = getConfiguration("yaas.security.oauth_url")

  val documentUrl: String = getConfiguration("yaas.document.url")


  val docMaxFailures: Int = getConfigurationInt("yaas.document.max_failures")

  val docCallTimeout: FiniteDuration = getDuration("yaas.document.call_timeout")

  val docResetTimeout: FiniteDuration = getDuration("yaas.document.reset_timeout")


  val oauthMaxFailures: Int = getConfigurationInt("yaas.security.oauth_max_failures")

  val oauthCallTimeout: FiniteDuration = getDuration("yaas.security.oauth_call_timeout")

  val oauthResetTimeout: FiniteDuration = getDuration("yaas.security.oauth_reset_timeout")

  val basicAuthCredentials: Option[Seq[String]] = config.getStringSeq("yaas.security.basic_auth_credentials")

  private def getConfiguration(key: String): String =
    config.getString(key).getOrElse(reportConfigurationError(key))

  private def getConfigurationInt(key: String): Int =
    config.getInt(key).getOrElse(reportConfigurationError(key))

  private def getDuration(key: String): FiniteDuration =
    Duration(config.getMilliseconds(key).getOrElse(reportConfigurationError(key)), MILLISECONDS)

  def reportConfigurationError(key: String): Nothing =
    throw new IllegalArgumentException(s"$key not configured")

}
