package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpMethod, HttpMethods }
import co.adhoclabs.akka.http.contrib.ContribConfig

import scala.concurrent.duration._
import scredis.{ Redis, RedisConfig }

/**
  * Created by yeghishe on 6/13/16.
  */
trait ConfigMetricThrottleSettings extends MetricThrottleSettings with ContribConfig {
  import scala.collection.JavaConverters._

  private lazy val enabled     = throttleConfig.getBoolean("enabled")
  private lazy val storeConfig = throttleConfig.getString("default-store")
  private lazy val endpointsConfig =
    throttleConfig.getConfigList("endpoints").asScala.toList

  override lazy val store: MetricStore = storeConfig match {
    case "redis" ⇒
      new RedisMetricStore(
        Redis(RedisConfig(throttleConfig)),
        throttleConfig.getConfig("redis").getString("namespace")
      )
    case "caffeine" ⇒
      new CaffeineCacheMetricStore(
        throttleConfig.getConfig("caffeine").getString("namespace")
      )
  }

  override lazy val endpoints: List[ThrottleEndpoint] = endpointsConfig.map { c ⇒
    val throttlePeriod = if (c.hasPath("throttle-period")) {
      Some(Duration(c.getDuration("throttle-period", SECONDS), SECONDS))
    } else {
      None
    }

    ThrottleEndpoint(
      RegexEndpoint(methodFromString(c.getString("method")), c.getString("pattern")),
      ThrottleDetails(Duration(c.getDuration("window", SECONDS), SECONDS),
                      c.getInt("allowed-calls"),
                      throttlePeriod)
    )
  } filter (_ ⇒ enabled)

  private def methodFromString(method: String): HttpMethod =
    method.toUpperCase match {
      case "CONNECT" ⇒ HttpMethods.CONNECT
      case "DELETE"  ⇒ HttpMethods.DELETE
      case "GET"     ⇒ HttpMethods.GET
      case "HEAD"    ⇒ HttpMethods.HEAD
      case "OPTIONS" ⇒ HttpMethods.OPTIONS
      case "PATCH"   ⇒ HttpMethods.PATCH
      case "POST"    ⇒ HttpMethods.POST
      case "PUT"     ⇒ HttpMethods.PUT
      case "TRACE"   ⇒ HttpMethods.TRACE
    }
}
