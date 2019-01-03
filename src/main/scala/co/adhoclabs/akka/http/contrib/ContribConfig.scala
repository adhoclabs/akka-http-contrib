package co.adhoclabs.akka.http.contrib

import com.typesafe.config.{ Config, ConfigFactory }

/**
  * Created by yeghishe on 6/10/16.
  */
trait ContribConfig {
  lazy val config: Config         = ConfigFactory.load().getConfig("akka.http.contrib")
  lazy val throttleConfig: Config = config.getConfig("throttle")
}

object ContribConfig extends ContribConfig
