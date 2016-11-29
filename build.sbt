name := "scala-wishlist"

version := "0.1.0-SNAPSHOT"

scapegoatVersion := "1.2.1"

val sdkVersion = "4.11.2"

import de.johoop.findbugs4sbt.FindBugs._

findbugsSettings

val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq("org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "com.github.tomakehurst" % "wiremock" % "2.3.1" % Test,
    "com.sap.cloud.yaas.service-sdk" % "service-sdk-logging" % sdkVersion,
    "com.sap.cloud.yaas.service-sdk" % "service-sdk-pattern-support" % sdkVersion
  ),
  baseUrl in versioneye := "https://versioneye.hybris.com"
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, VersionEyePlugin)
  .disablePlugins(PlayLogback)
  .settings(commonSettings: _*)
  .settings(// Enable injected generator
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(ws, filters, cache),
    baseUrl in versioneye := "https://versioneye.hybris.com"
  )
