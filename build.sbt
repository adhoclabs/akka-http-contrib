import org.scalastyle.sbt.ScalastylePlugin.{ projectSettings => scalaStyleSettings }
import scalariform.formatter.preferences._

organization := "co.adhoclabs"

name := "akka-http-contrib"

version := "0.0.3"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies ++= {
  val akkaVersion  = "2.4.6"

  Seq(
    "com.typesafe.akka"             %% "akka-http-core"                          % akkaVersion,
    "com.typesafe.akka"             %% "akka-http-experimental"                  % akkaVersion,
    "com.typesafe.akka"             %% "akka-http-spray-json-experimental"       % akkaVersion,
    "com.typesafe.akka"             %% "akka-http-testkit"                       % akkaVersion % "test",
    "net.debasishg"                 %  "redisclient_2.11"                        % "2.13",
    "org.scalatest"                 %% "scalatest"                               % "2.2.3"     % "test",
    "org.mockito"                   %  "mockito-all"                             % "1.10.19"   % "test"
  )
}

coverageMinimum := 90

coverageFailOnMinimum := false

coverageAggregate := true

coverageEnabled.in(Test, test) := true
