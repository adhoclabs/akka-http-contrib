package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpMethods
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scredis.serialization.{Reader, Writer}

/**
  * Created by yeghishe on 6/13/16.
  */
class RedisMetricStoreTest
    extends WordSpecLike
    with Matchers
    with MockFactory
    with ScalaFutures {
  import scala.concurrent.ExecutionContext.Implicits.global
  import HttpMethods._
  import RedisMetricStore._

  private val namespace = "abc:"
  private val redis = mock[RedisTT]
  private val redisMetricStore = new RedisMetricStore(redis, namespace)
  private val throttleEndpoint =
    ThrottleEndpoint(RegexEndpoint(GET, "p"), ThrottleDetails(1 minute, 10))
  private val url = "/test"
  private val endpointKey = s"${namespace}195c099ecd65b57571d5e220a85e8ad5"

  "RedisMetricStore" when {
    "keyForEndpoint" should {
      "return key with namespace" in {
        redisMetricStore.keyForEndpoint(throttleEndpoint, url) should equal(
          endpointKey)
      }
    }

    "get" should {
      "return the value from redis" in {
        import scredis.serialization.Implicits.longReader
        (redis
          .get[Long](_: String)(_: Reader[Long]))
          .expects(endpointKey, *) returning Future(Some(10))

        redisMetricStore.get(throttleEndpoint, url).futureValue should equal(10)
      }

      "return 0 if value isn't in redis" in {
        import scredis.serialization.Implicits.longReader
        (redis
          .get[Long](_: String)(_: Reader[Long]))
          .expects(endpointKey, *) returning Future(None)

        redisMetricStore.get(throttleEndpoint, url).futureValue should equal(0)
      }
    }

    "set" should {
      "set value with expiration in redis" in {
        val count = 10L
        (redis
          .pSetEX[Long](_: String, _: Long, _: Long)(_: Writer[Long]))
          .expects(endpointKey,
                   count,
                   throttleEndpoint.throttleDetails.window.toMillis,
                   *) returning Future(())

        redisMetricStore
          .set(throttleEndpoint, url, count)
          .futureValue should be(())
      }
    }

    "incr" should {
      "increment the value in redis if it exists" in {
        (redis.exists _).expects(endpointKey) returning Future(true)
        (redis.incr _).expects(endpointKey) returning Future(
          throttleEndpoint.throttleDetails.allowedCalls / 2)

        redisMetricStore.incr(throttleEndpoint, url).futureValue should be(())
      }

      "set the value to 1 in redis if does not exist" in {
        (redis.exists _).expects(endpointKey) returning Future(false)
        (redis
          .pSetEX[Long](_: String, _: Long, _: Long)(_: Writer[Long]))
          .expects(endpointKey,
                   1L,
                   throttleEndpoint.throttleDetails.window.toMillis,
                   *) returning Future(())

        redisMetricStore.incr(throttleEndpoint, url).futureValue should be(())
      }

      "change the expiration if allowedCalls is reached and throttlePeriod is present" in {
        val throttlePeriod = 2 hours
        val endpoint = throttleEndpoint.copy(
          throttleDetails = throttleEndpoint.throttleDetails.copy(
            throttlePeriod = Some(throttlePeriod))
        )

        (redis.exists _).expects(endpointKey) returning Future(true)
        (redis.incr _).expects(endpointKey) returning Future(
          endpoint.throttleDetails.allowedCalls)
        (redis.pExpire _)
          .expects(endpointKey, throttlePeriod.toMillis) returning Future(true)

        redisMetricStore.incr(endpoint, url).futureValue should be(())
      }
    }
  }

}
