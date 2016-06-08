name := "scala-wishlist"

version := "0.1.0-SNAPSHOT"

val sdkVersion = "4.7.0"


val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq("org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "com.github.tomakehurst" % "wiremock" % "1.58" % Test,
    "com.sap.cloud.yaas.service-sdk" % "service-sdk-logging" % sdkVersion,
    "com.sap.cloud.yaas.service-sdk" % "service-sdk-pattern-support" % sdkVersion
  )
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .disablePlugins(PlayLogback)
  .settings(commonSettings: _*)
  .settings(// Enable injected generator
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(ws, filters, cache))
