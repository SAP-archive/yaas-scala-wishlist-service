name := "scala-wishlist"

version := "0.1.0-SNAPSHOT"

val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq("org.scalatestplus" %% "play" % "1.4.0" % Test,
    "com.github.tomakehurst" % "wiremock" % "1.58" % Test)
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(// Enable injected generator
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(ws, cache))
