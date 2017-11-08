import org.scalastyle.sbt.ScalastylePlugin.{projectSettings => scalaStyleSettings}

organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "1.0.0"

scalaVersion := "2.11.11"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= {
  Seq(
    "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
}

libraryDependencies ++= {
  val akkaHttpV = "10.0.10"
  val scalaTestV = "3.0.3"
  val scalaMockV = "3.2.2"

  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",

    "com.github.etaty" %% "rediscala" % "1.8.0",

    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % "test"
  )
}

