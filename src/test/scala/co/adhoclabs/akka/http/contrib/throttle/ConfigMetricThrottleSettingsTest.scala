package co.adhoclabs.akka.http.contrib.throttle

import com.typesafe.config.{ Config, ConfigFactory }
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.ExecutionContext
//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import akka.http.scaladsl.model.HttpMethods

import scala.language.postfixOps

/**
  * Created by yeghishe on 6/13/16.
  */
class ConfigMetricThrottleSettingsTest extends WordSpecLike with Matchers with MockFactory {
  import scala.concurrent.ExecutionContext.Implicits.global
  import HttpMethods._

  private def getSettings(configString: String): ConfigMetricThrottleSettings =
    new ConfigMetricThrottleSettings {
      implicit override val executor: ExecutionContext =
        scala.concurrent.ExecutionContext.Implicits.global
      override lazy val throttleConfig: Config =
        ConfigFactory.parseString(configString).getConfig("throttle")
    }

  "ConfigMetricThrottleSettings" when {
    "endpoints" should {
      "should ne nil if not endpoints specified in config" in {
        val config =
          """
            |throttle {
            |  enabled = true
            |  endpoints = []
            |}
          """.stripMargin
        getSettings(config).endpoints should be(Nil)
      }

      "should be nil if there are endpoints in config but enabled is set to false" in {
        val config =
          """
            |throttle {
            |  enabled = false
            |  endpoints = [
            |   {
            |     method = "GET"
            |     pattern = "my pattern"
            |     window = 1 m
            |     allowed-calls = 10
            |     throttle-period = 2 h
            |   }
            |  ]
            |}
          """.stripMargin
        getSettings(config).endpoints should be(Nil)
      }

      "should construct endpoints from config" in {
        val config =
          """
            |throttle {
            |  enabled = true
            |  endpoints = [
            |   {
            |     method = "GET"
            |     pattern = "my pattern1"
            |     window = 1 m
            |     allowed-calls = 10
            |     throttle-period = 11 h
            |   },
            |   {
            |     method = "post"
            |     pattern = "my pattern2"
            |     window = 2 m
            |     allowed-calls = 20
            |   },
            |  ]
            |}
          """.stripMargin
        val endpoints = List(
          ThrottleEndpoint(RegexEndpoint(GET, "my pattern1"),
                           ThrottleDetails(1 minute, 10, Some(11 hours))),
          ThrottleEndpoint(RegexEndpoint(POST, "my pattern2"), ThrottleDetails(2 minutes, 20))
        )
        getSettings(config).endpoints should equal(endpoints)
      }
    }

    "store" should {
      "create the store chosen in config" in {
        val config =
          """
            |throttle {
            |  default-store = "redis"
            |
            |  redis {
            |    host = "localhost"
            |    port = 6379
            |    database = 0
            |    namespace = "throttle:"
            |  }
            |}
          """.stripMargin
        getSettings(config).store shouldBe a[RedisMetricStore]
      }
    }
  }
}
