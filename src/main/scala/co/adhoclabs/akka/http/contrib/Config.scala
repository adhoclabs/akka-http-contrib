package co.adhoclabs.akka.http.contrib

import com.typesafe.config.ConfigFactory

/**
  * Created by yeghishe on 6/10/16.
  */
trait Config {
  private val config = ConfigFactory.load().getConfig("akka.http.contrib")
  val throttleConfig = config.getConfig("throttle")
}

object Config extends Config
