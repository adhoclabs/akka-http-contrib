package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpMethods
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

class CaffeineCacheMetricStoreTest
    extends WordSpecLike
    with Matchers
    with MockFactory
    with ScalaFutures {
  import HttpMethods._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val namespace = "abc:"
  private val metricStoreMock = mock[CaffeineCacheMetricStore]
  val metricStore = new CaffeineCacheMetricStore(namespace)
  private val throttleEndpoint =
    ThrottleEndpoint(RegexEndpoint(GET, "p"), ThrottleDetails(1 second, 1)) // TODO - check matching
  private val url = "/test"
  private val endpointKey = s"${namespace}195c099ecd65b57571d5e220a85e8ad5"

  "CaffeineCacheMetricStore" when {
    "keyForEndpoint" should {
      "return key with namespace" in {
        metricStore.keyForEndpoint(throttleEndpoint, url) should equal(
          endpointKey)
      }
    }

    "get" should {
      "return the value from cache - mock" in {
        (metricStoreMock.get _)
          .expects(throttleEndpoint, url) returning Future(10L)

        metricStoreMock.get(throttleEndpoint, url).futureValue should equal(10)
      }

      "return the value from cache" in {
        val count = 3
        metricStore.set(throttleEndpoint, url, count).futureValue
        metricStore.get(throttleEndpoint, url).futureValue should equal(count)
      }

      "return 0 if value isn't in cache" in {
        metricStore.get(throttleEndpoint, url).futureValue should equal(0)
      }
    }

    "set" should {
      "set value with expiration in cache" in {
        val count = 10L
        metricStore.set(throttleEndpoint, url, count).futureValue
        metricStore.get(throttleEndpoint, url).futureValue should equal(count)
        Thread.sleep(throttleEndpoint.throttleDetails.window.toMillis)
        metricStore.get(throttleEndpoint, url).futureValue should equal(0)
      }
    }

    "incr" should {
      "increment the value in redis if it exists" in {
//        (metricStore.exists _).expects(endpointKey) returning Future(true)
//        (metricStore.incr _).expects(endpointKey) returning Future(
//          throttleEndpoint.throttleDetails.allowedCalls / 2)

        metricStore.incr(throttleEndpoint, url).futureValue should be(())
        metricStore.incr(throttleEndpoint, url).futureValue should be(())
        metricStore.incr(throttleEndpoint, url).futureValue should be(())
        metricStore.incr(throttleEndpoint, url).futureValue should be(())
      }

//      "set the value to 1 in redis if does not exist" in {
//        (metricStore.exists _).expects(endpointKey) returning Future(false)
//        (metricStore
//          .pSetEX[Long](_: String, _: Long, _: Long)(_: Writer[Long]))
//          .expects(endpointKey,
//                   1L,
//                   throttleEndpoint.throttleDetails.window.toMillis,
//                   *) returning Future(())
//
//        metricStore.incr(throttleEndpoint, url).futureValue should be(())
//      }

//      "change the expiration if allowedCalls is reached and throttlePeriod is present" in {
//        val throttlePeriod = 2 hours
//        val endpoint = throttleEndpoint.copy(
//          throttleDetails = throttleEndpoint.throttleDetails.copy(
//            throttlePeriod = Some(throttlePeriod))
//        )
//
//        (metricStore.exists _).expects(endpointKey) returning Future(true)
//        (metricStore.incr _).expects(endpointKey) returning Future(
//          endpoint.throttleDetails.allowedCalls)
//        (metricStore.pExpire _)
//          .expects(endpointKey, throttlePeriod.toMillis) returning Future(true)
//
//        metricStore.incr(endpoint, url).futureValue should be(())
//      }
    }
  }

}
