name := "scala-wishlist"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(    // Enable injected generator
  routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(ws, cache))
