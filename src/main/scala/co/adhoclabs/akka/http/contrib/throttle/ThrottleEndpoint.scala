package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpRequest

import scala.concurrent.duration.Duration

case class Endpoint(method: String, uri: String) {
  def matches(request: HttpRequest): Boolean = {
    val uriMatches = uri
      .replaceAll("\\{[a-zA-Z0-9]*\\}", "[a-zA-Z0-9]*").r
      .pattern
      .matcher(request.uri.path.toString()).matches

    (method.toUpperCase() == request.method.value) && uriMatches
  }
}

case class Expiration(window: Duration, allowedCalls: Long)

case class ThrottleEndpoint(endpoint: Endpoint, expiration: Expiration)