package co.adhoclabs.akka.http.contrib.throttle

import java.time.Instant

import scalacache._
import scalacache.caffeine._

import scala.concurrent.Future

class CaffeineCacheMetricStore(namespace: String = "") extends MetricStore {

  import concurrent.ExecutionContext.Implicits.global
  import scalacache.modes.scalaFuture._
  val cache = CaffeineCache[Long]

  override def keyForEndpoint(throttleEndpoint: ThrottleEndpoint,
                              url: String): String =
    s"$namespace${super.keyForEndpoint(throttleEndpoint, url)}"

  /**
    * get should return the current value for current window or zero. Meaning...
    * if there is no value it should return 0
    * if there is value and window changed it should return 0
    * if there is value and current window didn't change it should return that value
    *
    * @return
    */
  override def get(throttleEndpoint: ThrottleEndpoint,
                   url: String): Future[Long] = {
    val k = keyForEndpoint(throttleEndpoint, url)
    cache.doGet(k).map(_.getOrElse(0))
  }

  /**
    * Same rules apply here as in get. incr should set the value to current value + 1 for current window or 1.
    *
    * @param throttleEndpoint
    */
  override def incr(throttleEndpoint: ThrottleEndpoint,
                    url: String): Future[Unit] = {

    val key = keyForEndpoint(throttleEndpoint, url)
    val expires =
      Instant.now().plusMillis(throttleEndpoint.throttleDetails.window.toMillis)
    val v = cache.underlying.get(key, {
      case k: String =>
        Entry[Long](1, Some(expires))
    })

    val count = v.value

    throttleEndpoint.throttleDetails.throttlePeriod
      .filter(_ ⇒ count >= throttleEndpoint.throttleDetails.allowedCalls) //
      //  cache.pExpire(key, p.toMillis)
      .map(p ⇒ cache.doPut(key, count + 1, Some(p)))
      .getOrElse(Future.successful(false))
      .map(_ => ())
  }

  override def set(throttleEndpoint: ThrottleEndpoint,
                   url: String,
                   count: Long): Future[Unit] = {
    val k = keyForEndpoint(throttleEndpoint, url)
    val ttl = throttleEndpoint.throttleDetails.window
    cache.doPut(k, count, Some(ttl)).map(_ => ())
  }

}
