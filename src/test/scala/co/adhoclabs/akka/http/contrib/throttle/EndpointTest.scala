package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, Uri }
import org.scalatest.FunSuite

class EndpointTest extends FunSuite {
  test("matching url should return true when requested uri matches the endpoint uri") {
    val endpoint = Endpoint("GET", "/burner/{burnerid}/setting")
    val req = HttpRequest(HttpMethods.GET, Uri("http:///burner/someBurnerId/setting"))
    assert(endpoint.matches(req) === true)
  }

  test("matching url should return false when requested uri does not matches the endpoint uri") {
    val endpoint = Endpoint("GET", "/foo/{burnerid}/bar")
    val req = HttpRequest(HttpMethods.GET, Uri("http:///burner/someBurnerId/setting"))
    assert(endpoint.matches(req) === false)
  }

  test("matching url should return false when requested uri matches the endpoint uri but the method does not.") {
    val endpoint = Endpoint("POST", "/burner/{burnerid}/setting")
    val req = HttpRequest(HttpMethods.GET, Uri("http:///burner/someBurnerId/setting"))
    assert(endpoint.matches(req) === false)
  }
}
