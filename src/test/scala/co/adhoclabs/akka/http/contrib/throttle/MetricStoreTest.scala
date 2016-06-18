package co.adhoclabs.akka.http.contrib.throttle

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{ Matchers, PropSpec }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.http.scaladsl.model.HttpMethods

/**
 * Created by yeghishe on 6/10/16.
 */
class MetricStoreTest extends PropSpec with TableDrivenPropertyChecks with Matchers with MetricStore {
  import HttpMethods._

  private val examples = Table(
    ("endpoint", "url", "md5"),
    (ThrottleEndpoint(RegexEndpoint(GET, "^/hello[/]?$"), ThrottleDetails(1 hour, 10)), "/hello", "507dc6276acb6adeef76822b63d0e9e5"),
    (ThrottleEndpoint(RegexEndpoint(POST, "^/hello[/]?$"), ThrottleDetails(1 hour, 10)), "/hello", "c2980da021029bea33bb0d1c7bb0a202"),
    (ThrottleEndpoint(RegexEndpoint(GET, "^/hello/([-\\w]*)[/]?"), ThrottleDetails(1 hour, 10)), "/hello/10", "7a490b0204113cf1449bf81938884c8c")
  )

  override def get(throttleEndpoint: ThrottleEndpoint, url: String): Future[Long] = ???
  override def set(throttleEndpoint: ThrottleEndpoint, url: String, count: Long): Future[Unit] = ???
  override def incr(throttleEndpoint: ThrottleEndpoint, url: String): Future[Unit] = ???

  property("should calculate md5 right") {
    forAll(examples) { (endpoint, url, md5) ⇒
      keyForEndpoint(endpoint, url) should equal(md5)
    }
  }
}
