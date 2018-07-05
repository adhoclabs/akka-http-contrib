package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpMethod, HttpRequest, RemoteAddress }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.matching.Regex

case class RegexEndpoint(method: HttpMethod, pattern: String) extends Endpoint {

  override def getIdentifier(remoteAddress: RemoteAddress, url: String): String =
    //    s"${remoteAddress.toIP.fold("")(_.ip.getHostAddress)}${method.value} $url"
    s"$pattern ${method.value} ${remoteAddress.toIP.fold("")(_.ip.getHostAddress)}" // We enforce limits per IP per rule

  override def matches(request: HttpRequest)(implicit ec: ExecutionContext): Future[Boolean] =
    Future(method == request.method && new Regex(pattern).pattern.matcher(request.uri.path.toString()).matches())
}
