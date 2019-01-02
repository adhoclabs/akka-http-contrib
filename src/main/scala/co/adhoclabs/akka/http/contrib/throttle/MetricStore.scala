package co.adhoclabs.akka.http.contrib.throttle

import java.security.MessageDigest

import scala.concurrent.Future

trait MetricStore {

  // TODO - extend to use more than the target URL - source header token or IP
  def keyForEndpoint(throttleEndpoint: ThrottleEndpoint, url: String): String =
    MessageDigest
      .getInstance("MD5")
      .digest(throttleEndpoint.endpoint.getIdentifier(url).getBytes)
      .map(0xFF & _)
      .map("%02x".format(_))
      .mkString

  /**
    * get should return the current value for current window or zero. Meaning...
    * if there is no value it should return 0
    * if there is value and window changed it should return 0
    * if there is value and current window didn't change it should return that value
    *
    * @return
    */
  def get(throttleEndpoint: ThrottleEndpoint, url: String): Future[Long]

  /**
    * Same rules apply here as in get. incr should set the value to current value + 1 for current window or 1.
    *
    * @param throttleEndpoint
    */
  def incr(throttleEndpoint: ThrottleEndpoint, url: String): Future[Unit]

  def set(throttleEndpoint: ThrottleEndpoint, url: String, count: Long): Future[Unit]
}
