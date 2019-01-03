package co.adhoclabs.akka.http.contrib

import com.typesafe.config.{ Config, ConfigFactory }

/**
  * Created by yeghishe on 6/10/16.
  */
trait ContribConfig {
  val config: Config         = ConfigFactory.load().getConfig("akka.http.contrib")
  val throttleConfig: Config = config.getConfig("throttle")
}

object ContribConfig extends ContribConfig
