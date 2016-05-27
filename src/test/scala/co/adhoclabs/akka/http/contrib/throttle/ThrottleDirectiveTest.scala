package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpRequest, StatusCodes }
import akka.http.scaladsl.server.directives.{ MethodDirectives, PathDirectives, RouteDirectives }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSuite }

/*
 Dummy service to mock and to use in the test to check if the throttle directive is working.
 */
class SomeService {
  def doSomething() = 1
}

class ThrottleDirectiveTest extends FunSuite
    with MockitoSugar
    with ScalatestRouteTest
    with ThrottleDirective
    with BeforeAndAfter {
  import PathDirectives._
  import RouteDirectives._
  import MethodDirectives._
  private val service = mock[SomeService]
  private val settings = mock[MetricThrottleSettings]

  def throttledRoute = throttle(settings) {
    pathEndOrSingleSlash {
      get {
        complete {
          service.doSomething()
          "done"
        }
      }
    }
  }

  before {
    reset(service, settings)
  }

  when(service.doSomething()).thenReturn(1)

  test("Throttle should reject request if limit is reached") {
    when(settings.shouldThrottle(anyObject())).thenReturn(true)

    Get("/") ~> throttledRoute ~> check {
      assert(status === StatusCodes.TooManyRequests)
      assert(responseAs[String] === "The user has sent too many requests in a given amount of time.")
      verify(service, times(0)).doSomething()
    }
  }

  test("Throttle should not reject request if limit is not reached") {
    when(settings.shouldThrottle(anyObject())).thenReturn(false)

    Get("/") ~> throttledRoute ~> check {
      assert(status === StatusCodes.OK)
      assert(responseAs[String] === "done")
      verify(service, times(1)).doSomething()
      verify(settings, times(1)).onExecute(anyObject[HttpRequest])
    }
  }
}
