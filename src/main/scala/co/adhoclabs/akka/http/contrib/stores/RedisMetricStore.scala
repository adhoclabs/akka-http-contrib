package co.adhoclabs.akka.http.contrib.stores

import akka.http.scaladsl.model.HttpRequest
import co.adhoclabs.akka.http.contrib.throttle.ThrottleEndpoint
import com.redis.RedisClientPool

class RedisMetricStore(val redis: RedisClientPool) extends MetricStore {
  /**
   * get should return the current value for current window or zero. Meaning...
   * if there is no value it should return 0
   * if there is value and window changed it should return 0
   * if there is value and current window didn't change it should return that value
   *
   * @return
   */
  override def get(endpoint: ThrottleEndpoint, url: String): Long = {
    val key = keyForEndpoint(endpoint, url)
    redis.withClient(_.get(key)).map(_.toLong).getOrElse(0)
  }

  override def reset(endpoint: ThrottleEndpoint, url: String): Unit = {
    val key = keyForEndpoint(endpoint, url)
    val expiration = (System.currentTimeMillis() + endpoint.expiration.window.toMillis) / 1000
    redis.withClient(_.setex(key, expiration.toInt, 0))
  }

  /**
   * Same rules apply here as in get.
   * incr should set the value to current value + 1 for current window or 1.
   *
   * @param endpoint
   */
  override def incr(endpoint: ThrottleEndpoint, url: String): Unit = {
    val key = keyForEndpoint(endpoint, url)

    if (get(endpoint, url) > 0L) {
      redis.withClient(_.incr(key))
    } else {
      val expiration = (System.currentTimeMillis() + endpoint.expiration.window.toMillis) / 1000
      redis.withClient(_.setex(key, expiration.toInt, 1))
    }
  }
}
