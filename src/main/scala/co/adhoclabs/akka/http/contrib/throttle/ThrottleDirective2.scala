package co.adhoclabs.akka.http.contrib.throttle

import java.security.MessageDigest

import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import akka.http.scaladsl.server.{ Directive, Directive0 }
import akka.http.scaladsl.server.directives.{ BasicDirectives, RouteDirectives }

import scala.concurrent.duration.Duration

/**
 * Created by yeghishe on 5/26/16.
 */

case class Endpoint(method: String, uri: String) {
  // TODO: provide implementation
  // uri is gonna be like `/burner/{burnerid}/setting` and url is gonna be `/burner/abc/setting`. regex to help.
  def matches(request: HttpRequest): Boolean = ???
}
case class Expiration(window: Duration, allowedCalls: Long)
case class ThrottleEndpoint(endpoint: Endpoint, expiration: Expiration)

trait ThrottleSettings {
  def shouldThrottle(request: HttpRequest): Boolean
  def onExecute(request: HttpRequest): Unit
}

object MetricThrottleSettings {
  def fromConfig: MetricThrottleSettings = {
    new MetricThrottleSettings {
      // TODO: Store will be redis if config says it's redis.
      override def store: MetricStore = ???

      // TODO: populate from config
      // config should also have explicit enabled property(defaulting to false). if enabled is false list must be empty.
      override def endpoints: List[ThrottleEndpoint] = ???
    }
  }
}

trait MetricThrottleSettings extends ThrottleSettings {
  def store: MetricStore
  def endpoints: List[ThrottleEndpoint]

  override def shouldThrottle(request: HttpRequest): Boolean = endpoints
    .filter(_.endpoint.matches(request))
    // TODO: make sure `request.uri.path.toString` returns what we want.
    .exists(te ⇒ store.get(te, request.uri.path.toString) <= te.expiration.allowedCalls)

  override def onExecute(request: HttpRequest): Unit = endpoints
    .filter(_.endpoint.matches(request))
    // TODO: make sure `request.uri.path.toString` returns what we want.
    .foreach(te ⇒ store.incr(te, request.uri.path.toString))
}

trait MetricStore {
  def keyForEndpoint(endpoint: ThrottleEndpoint): String = {
    val d = MessageDigest.getInstance("MD5")
    d.update("".toCharArray.map(_.toByte))
    new String(d.digest().map(_.toChar))
  }

  /**
   * get should return the current value for current window or zero. Meaning...
   *   if there is no value it should return 0
   *   if there is value and window changed it should return 0
   *   if there is value and current window didn't change it should return that value
   *
   * @param endpoint
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

trait ThrottleDirective2 {
  import BasicDirectives._
  import RouteDirectives._

  def throttle(settings: ThrottleSettings = MetricThrottleSettings.fromConfig): Directive0 =
    extractRequest.flatMap { r ⇒
      if (settings.shouldThrottle(r)) {
        complete(StatusCodes.TooManyRequests)
      } else {
        settings.onExecute(r)
        Directive.Empty
      }
    }
}

object ThrottleDirective2 extends ThrottleDirective2
