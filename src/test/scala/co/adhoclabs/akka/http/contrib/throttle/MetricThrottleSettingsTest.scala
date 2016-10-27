package co.adhoclabs.akka.http.contrib.throttle

import java.net.InetAddress

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RemoteAddress}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by yeghishe on 6/13/16.
 */
class MetricThrottleSettingsTest extends WordSpecLike with Matchers with ScalaFutures with MockFactory {
  import scala.concurrent.ExecutionContext.Implicits.global

  private case class TestEndpoint(name: String) extends Endpoint {
    override def matches(request: HttpRequest)(implicit ec: ExecutionContext): Future[Boolean] =
      Future(request.uri.path.toString().contains(name))
    override def getIdentifier(remoteAddress: RemoteAddress, url: String): String = url
  }

  private val localHost = RemoteAddress(InetAddress.getLocalHost)
  private val throttleDetails = ThrottleDetails(1 hour, 10)
  private val metricStore = mock[MetricStore]
  private val endpoint = ThrottleEndpoint(TestEndpoint("test"), throttleDetails)
  private val metricThrottleSettings = new MetricThrottleSettings {
    override implicit val executor: ExecutionContext = implicitly
    override val store: MetricStore = metricStore
    override val endpoints: List[ThrottleEndpoint] = List(endpoint)
  }

  "MetricThrottleSettingsTest" when {
    "shouldThrottle" should {
      "return false if no matching endpoint is found" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = "/foo")

        metricThrottleSettings.shouldThrottle(localHost, request).futureValue should be(false)
      }

      "return false if matching endpoint is found and metric store has count lower than max count" in {
        val url = "/test"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        (metricStore.get _).expects(endpoint, localHost, url) returning Future(throttleDetails.allowedCalls - 1)

        metricThrottleSettings.shouldThrottle(localHost, request).futureValue should be(false)
      }

      "return true if matching endpoint is found and metric store has count higher or equal to max count" in {
        val url = "/test"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        (metricStore.get _).expects(endpoint, localHost, url) returning Future(throttleDetails.allowedCalls)

        metricThrottleSettings.shouldThrottle(localHost, request).futureValue should be(true)
      }
    }

    "onExecute" should {
      "do nothing if no matching endpoint is found" in {
        val request = HttpRequest(method = HttpMethods.GET, uri = "/foo")

        metricThrottleSettings.onExecute(localHost, request).futureValue should be(())
      }

      "call metric store to increment the metric if matching endpoint is found" in {
        val url = "/test"
        val request = HttpRequest(method = HttpMethods.GET, uri = url)
        (metricStore.incr _).expects(endpoint, localHost, url) returning Future(())

        metricThrottleSettings.onExecute(localHost, request).futureValue should be(())
      }
    }

    "fromConfig" should {
      "create ConfigMetricThrottleSettings" in {
        MetricThrottleSettings.fromConfig shouldBe a[ConfigMetricThrottleSettings]
      }
    }
  }
}
