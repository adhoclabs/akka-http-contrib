package co.adhoclabs.akka.http.contrib

import akka.http.scaladsl.model.{HttpRequest, RemoteAddress}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

/**
 * Created by yeghishe on 6/10/16.
 */
package object throttle {
  trait Endpoint {
    def matches(request: HttpRequest)(implicit ec: ExecutionContext): Future[Boolean]
    def getIdentifier(remoteAddress: RemoteAddress, url: String): String
  }

  case class ThrottleDetails(window: Duration, allowedCalls: Long, throttlePeriod: Option[Duration] = None)
  case class ThrottleEndpoint(endpoint: Endpoint, throttleDetails: ThrottleDetails)

  trait ThrottleSettings {
    protected implicit def executor: ExecutionContext
    def shouldThrottle(remoteAddress: RemoteAddress, request: HttpRequest): Future[Boolean]
    def onExecute(remoteAddress: RemoteAddress, request: HttpRequest): Future[Unit]
  }
}
