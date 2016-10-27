package co.adhoclabs.akka.http.contrib.throttle

import java.net.InetAddress

import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RemoteAddress}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, PropSpec}
import org.scalatest.prop.TableDrivenPropertyChecks

/**
 * Created by yeghishe on 6/10/16.
 */
class RegexEndpointTest extends PropSpec with TableDrivenPropertyChecks with Matchers with ScalaFutures {
  import scala.concurrent.ExecutionContext.Implicits.global
  import HttpMethods._

  private val getIdentifierExamples = Table(
    ("endpoint", "url", "key"),
    (RegexEndpoint(GET, "^\\/hello[/]?$"), "/hello", "127.0.0.1 GET /hello"),
    (RegexEndpoint(POST, "^\\/hello[/]?$"), "/hello", "127.0.0.1 POST /hello")
  )

  private val matchesExamples = Table(
    ("endpoint", "method", "uri", "result"),
    (RegexEndpoint(GET, "^\\/hello[/]?$"), GET, "/hello", true),
    (RegexEndpoint(GET, "^\\/hello[/]?$"), GET, "/hellox", false),
    (RegexEndpoint(POST, "^\\/hello[/]?$"), GET, "/hello", false)
  )

  property("should construct the identifier right") {
    forAll(getIdentifierExamples) { (endpoint, url, key) ⇒
      endpoint.getIdentifier(RemoteAddress(InetAddress.getLocalHost), url) should equal(key)
    }
  }

  property("should tell if endpoint matches a request") {
    forAll(matchesExamples) { (endpoint, method, uri, result) ⇒
      endpoint.matches(HttpRequest(method = method, uri = uri)).futureValue should equal(result)
    }
  }
}
