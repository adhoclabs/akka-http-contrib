package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.server.directives.{ BasicDirectives, FutureDirectives, RouteDirectives, MiscDirectives }
import akka.http.scaladsl.server.{ Directive, Directive0 }

trait ThrottleDirective {
  import BasicDirectives._
  import FutureDirectives._
  import RouteDirectives._
  import MiscDirectives._

  def throttle(implicit settings: ThrottleSettings): Directive0 = (extractClientIP & extractRequest).tflatMap { case (ipAddress, request) ⇒
    onSuccess(settings.shouldThrottle(ipAddress, request)).flatMap { should ⇒
      if (should) {
        reject(TooManyRequestsRejection())
      } else {
        settings.onExecute(ipAddress, request)
        Directive.Empty
      }
    }
  }
}

object ThrottleDirective extends ThrottleDirective
