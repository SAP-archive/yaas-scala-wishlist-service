import de.heikoseeberger.sbtheader.HeaderKey._
import de.heikoseeberger.sbtheader.HeaderPattern

name := "scala-wishlist"

version := "0.1.0-SNAPSHOT"


val commonSettings = Seq(
  version := "1.0.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq("org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "com.github.tomakehurst" % "wiremock" % "1.58" % Test,
    "com.sap.cloud.yaas.service-sdk" % "service-sdk-pattern-support" % "4.7.0"),
  headers := Map(
    "scala" -> (
      HeaderPattern.cStyleBlockComment,
      """|/*
        | * [y] hybris Platform
        | *
        | * Copyright (c) 2000-2016 hybris AG
        | * All rights reserved.
        | *
        | * This software is the confidential and proprietary information of hybris
        | * ("Confidential Information"). You shall not disclose such Confidential
        | * Information and shall use it only in accordance with the terms of the
        | * license agreement you entered into with hybris.
        | */
        |""".stripMargin
      )
    )
)

lazy val root = (project in file(".")).enablePlugins(PlayScala)
  .settings(commonSettings: _*)
  .settings(// Enable injected generator
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(ws, filters, cache))
