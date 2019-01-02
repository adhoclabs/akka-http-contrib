package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpMethod, HttpRequest }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex

case class RegexEndpoint(method: HttpMethod, pattern: String) extends Endpoint {
  override def getIdentifier(url: String): String = s"${method.value} $url"
  override def matches(request: HttpRequest)(implicit ec: ExecutionContext): Future[Boolean] =
    Future(
      method == request.method && new Regex(pattern).pattern
        .matcher(request.uri.path.toString())
        .matches()
    )
}
