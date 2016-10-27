package co.adhoclabs.akka.http.contrib.throttle

import java.security.MessageDigest

import akka.http.scaladsl.model.RemoteAddress

import scala.concurrent.Future

trait MetricStore {
  def keyForEndpoint(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): String = MessageDigest
    .getInstance("MD5")
    .digest(throttleEndpoint.endpoint.getIdentifier(remoteAddress, url).getBytes)
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
  def get(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Long]

  /**
   * Same rules apply here as in get. incr should set the value to current value + 1 for current window or 1.
   *
   * @param throttleEndpoint
   */
  def incr(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Unit]

//  def set(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String, count: Long): Future[Unit]
}
