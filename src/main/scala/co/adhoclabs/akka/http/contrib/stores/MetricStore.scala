package co.adhoclabs.akka.http.contrib.stores

import java.security.MessageDigest
import java.util.Base64

import co.adhoclabs.akka.http.contrib.throttle.ThrottleEndpoint

trait MetricStore {
  def keyForEndpoint(endpoint: ThrottleEndpoint, url: String): String = {
    val d = MessageDigest.getInstance("MD5")
    val strToHash = endpoint.endpoint.method + url
    d.update(strToHash.toCharArray.map(_.toByte))
    Base64.getEncoder.encodeToString(d.digest())
  }

  /**
   * get should return the current value for current window or zero. Meaning...
   * if there is no value it should return 0
   * if there is value and window changed it should return 0
   * if there is value and current window didn't change it should return that value
   *
   * @return
   */
  def get(endpoint: ThrottleEndpoint, url: String): Long

  /**
   * Same rules apply here as in get. incr should set the value to current value + 1 for current window or 1.
   *
   * @param endpoint
   */
  def incr(endpoint: ThrottleEndpoint, url: String): Unit

  def reset(endpoint: ThrottleEndpoint, url: String): Unit
}