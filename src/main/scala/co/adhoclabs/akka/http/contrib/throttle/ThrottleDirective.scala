package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.server.directives.{BasicDirectives, FutureDirectives, RouteDirectives}
import akka.http.scaladsl.server.{Directive, Directive0}

trait ThrottleDirective {
  import BasicDirectives._
  import FutureDirectives._
  import RouteDirectives._

  def throttle(implicit settings: ThrottleSettings): Directive0 = extractRequest.flatMap { r ⇒
    onSuccess(settings.shouldThrottle(r)).flatMap { should ⇒
      if (should) {
        reject(TooManyRequestsRejection())
      } else {
        settings.onExecute(r)
        Directive.Empty
      }
    }
  }
}

object ThrottleDirective extends ThrottleDirective
