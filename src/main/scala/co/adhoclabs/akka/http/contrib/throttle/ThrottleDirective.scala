package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.{ BasicDirectives, RouteDirectives }
import akka.http.scaladsl.server.{ Directive, Directive0 }

trait ThrottleDirective {

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

object ThrottleDirective extends ThrottleDirective
