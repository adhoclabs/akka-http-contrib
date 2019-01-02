package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.{ HttpMethods, HttpRequest }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps

class MetricThrottleSettingsCaffeineTest extends WordSpecLike with Matchers with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  private case class TestEndpoint(name: String) extends Endpoint {
    override def matches(request: HttpRequest)(implicit ec: ExecutionContext): Future[Boolean] =
      Future(request.uri.path.toString().contains(name))(ec)

    override def getIdentifier(url: String): String = url
  }

  private val namespace       = "abc:"
  val metricStore             = new CaffeineCacheMetricStore(namespace)
  private val throttleDetails = ThrottleDetails(1 second, 1)
  private val url             = "/test"
  private val throttleEndpoint =
    ThrottleEndpoint(RegexEndpoint(GET, url), throttleDetails)
  private val endpointKey = s"${namespace}195c099ecd65b57571d5e220a85e8ad5"

  private val metricThrottleSettings = new MetricThrottleSettings {
    implicit override val executor: ExecutionContext =
      scala.concurrent.ExecutionContext.Implicits.global
    override val store: MetricStore                = metricStore
    override val endpoints: List[ThrottleEndpoint] = List(throttleEndpoint)
  }

  "MetricThrottleSettingsTest" when {
    "shouldThrottle" should {
      "return false if no matching endpoint is found" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = "/foo")
        metricThrottleSettings.shouldThrottle(request).futureValue should be(false)
      }

      "return false if matching endpoint is found and metric store has count lower than max count" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        metricThrottleSettings.shouldThrottle(request).futureValue should be(false)
      }

      "return true if matching endpoint is found and metric store has count higher or equal to max count" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        metricStore.set(throttleEndpoint, url, 10).futureValue
        metricThrottleSettings.shouldThrottle(request).futureValue should be(true)
      }
    }

    "onExecute" should {
      "do nothing if no matching endpoint" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = "/foo")
        metricThrottleSettings.onExecute(request).futureValue should be(())
      }

      "call metric store to increment the metric if matching endpoint is found" in {
        val url     = "/test"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        metricThrottleSettings.shouldThrottle(request).futureValue should be(false)
        metricThrottleSettings.onExecute(request).futureValue should be(())
        metricThrottleSettings.onExecute(request).futureValue should be(())
        metricThrottleSettings.shouldThrottle(request).futureValue should be(true)
      }
    }

    "fromConfig" should {
      "create ConfigMetricThrottleSettings" in {
        MetricThrottleSettings.fromConfig shouldBe a[ConfigMetricThrottleSettings]
      }
    }
  }
}
