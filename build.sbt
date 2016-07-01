import org.scalastyle.sbt.ScalastylePlugin.{ projectSettings => scalaStyleSettings }
import scalariform.formatter.preferences._

organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "0.0.6"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= {
  val akkaVersion = "2.4.6"
  val scalaTestV  = "3.0.0-M15"
  val scalaMockV  = "3.2.2"
  val scredisV    = "2.0.6"

  Seq(
    "com.typesafe.akka" %% "akka-http-core"                    % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental"            % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit"                 % akkaVersion % "test",
    "com.livestream"    %% "scredis"                           % scredisV,
    "org.scalatest"     %% "scalatest"                         % scalaTestV  % "test",
    "org.scalamock"     %% "scalamock-scalatest-support"       % scalaMockV  % "test"
  )
}

