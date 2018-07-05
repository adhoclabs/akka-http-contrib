organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "2.0.0"

scalaVersion := "2.12.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= {
  val akkaV = "2.5.11"
  val akkaHttpV = "10.1.0"
  val scalaTestV = "3.0.3"
  val scalaMockV = "3.6.0"

  Seq(
    "com.typesafe.akka" %% "akka-stream" % akkaV withSources(),
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpV % "test",

    "com.github.etaty" %% "rediscala" % "1.8.0",

    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock-scalatest-support" % scalaMockV % "test"
  )
}

