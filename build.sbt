import org.scalastyle.sbt.ScalastylePlugin.{projectSettings => scalaStyleSettings}
import scalariform.formatter.preferences._

organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "0.0.6"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers ++= {
  Seq(
    "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
}

libraryDependencies ++= {
  val akkaHttpVersion = "10.0.0"
  val scalaTestV = "3.0.0-M15"
  val scalaMockV = "3.2.2"

  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",

    "com.github.etaty" %% "rediscala" % "1.8.0",

    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % "test"
  )
}

