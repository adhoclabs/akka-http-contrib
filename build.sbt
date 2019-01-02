organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "0.0.7-SNAPSHOT"

scalaVersion := "2.12.8"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.8",
  "-encoding",
  "UTF-8",
  "-Yrangepos", // required by SemanticDB compiler plugin
  "-Ywarn-unused-import", // required by `RemoveUnused` rule
  "-Ywarn-adapted-args"
)

libraryDependencies ++= {
  val akkaVersion = "2.5.12"
  val akkaHttpVersion = "10.1.5"
  val scalaTestV = "3.0.5"
  val scalaMockV = "4.1.0"
  val scredisV = "2.2.3"
  val scalacacheV = "0.27.0"
  val scalaLoggingV = "3.9.0"
  val logbackV = "1.2.3"

  Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
    "com.github.scredis" %% "scredis" % scredisV,
    "com.github.cb372" %% "scalacache-caffeine" % scalacacheV,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
    "ch.qos.logback" % "logback-classic" % logbackV,
    "org.scalatest" %% "scalatest" % scalaTestV % "test",
    "org.scalamock" %% "scalamock" % scalaMockV % "test"
  )
}

lazy val scalafmtSettings =
  Seq(
    scalafmtOnCompile := true
  )

lazy val settings = scalafmtSettings
