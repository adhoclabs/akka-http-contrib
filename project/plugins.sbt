logLevel := Level.Warn

resolvers += Classpaths.sbtPluginReleases

//addSbtPlugin("com.sksamuel.scapegoat" %% "sbt-scapegoat" % "1.0.4")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
