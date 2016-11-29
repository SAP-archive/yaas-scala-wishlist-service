// The Typesafe repository

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

// Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.10")

// Eclipse plugin
addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.0.1")

// Scalastyle
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

// Scapegoat
addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")

// Scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

// versioneye
addSbtPlugin("com.versioneye" % "sbt-versioneye-plugin" % "0.2.0")

// findbugs4sbt
addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.4.0")
