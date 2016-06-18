package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpecLike }

import scala.concurrent.Future

/**
 * Created by yeghishe on 6/10/16.
 */
class ThrottleDirectiveTest extends WordSpecLike
    with Matchers
    with ScalaFutures
    with MockFactory
    with ScalatestRouteTest {

  import Directives._
  import ThrottleDirective._

  private trait Doer {
    def doWork(): String
  }

  private val doer = mock[Doer]
  private val throttleSettings = mock[ThrottleSettings]
  private val routes = throttle(throttleSettings) {
    pathPrefix("hello") {
      get {
        complete(doer.doWork())
      }
    }
  }

  "ThrottleDirective" when {
    "throttle" should {
      "throttle if shouldThrottle returns true" in {
        (throttleSettings.shouldThrottle _).expects(*).returns(Future(true))

        Get("/hello") ~> routes ~> check {
          status should be(StatusCodes.TooManyRequests)
          responseAs[String] should be("The user has sent too many requests in a given amount of time.")
        }
      }

      "NOT throttle if shouldThrottle returns false and allow innter route to execute" in {
        (doer.doWork _).expects().returns("done")
        (throttleSettings.shouldThrottle _).expects(*).returns(Future(false))
        (throttleSettings.onExecute _).expects(*).returns(Future(()))

        Get("/hello") ~> routes ~> check {
          status should be(StatusCodes.OK)
          responseAs[String] should be("done")
        }
      }
    }
  }
}
