package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import co.adhoclabs.akka.http.contrib.ThrottleConfiguration
import co.adhoclabs.akka.http.contrib.clients.StorageClient
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{ BeforeAndAfter, FunSuite }

/*
 Dummy service to mock and to use in the test to check if the throttle directive is working.
 */
class SomeService {
  def doSomething() = 1
}

trait ThrottledService extends ThrottleDirective {
  implicit def throttleConf: ThrottleConfiguration

  implicit def storageClient: StorageClient

  implicit def service: SomeService

  def throttledRoute = throttle {
    pathEndOrSingleSlash {
      get {
        complete {
          service.doSomething()
          "done"
        }
      }
    }
  }
}

class ThrottleDirectiveTest extends FunSuite
    with MockitoSugar
    with ScalatestRouteTest
    with ThrottledService
    with BeforeAndAfter {
  implicit override val throttleConf = mock[ThrottleConfiguration]
  implicit override val storageClient = mock[StorageClient]
  implicit override val service = mock[SomeService]

  before {
    reset(storageClient, throttleConf)
  }

  when(service.doSomething()).thenReturn(1)

  test("Throttle should reject request if limit is reached") {
    val key = "2184121"
    when(storageClient.getCount(key)).thenReturn(Option(51))
    when(throttleConf.maxCalls).thenReturn(50)

    Get("/") ~> throttledRoute ~> check {
      assert(status === StatusCodes.TooManyRequests)
      assert(responseAs[String] === "The user has sent too many requests in a given amount of time.")
      verify(service, times(0)).doSomething()
    }
  }

  test("Throttle should not reject request if limit is not reached") {
    val key = "2184121"
    when(storageClient.getCount(key)).thenReturn(Option(5))
    when(throttleConf.maxCalls).thenReturn(50)
    when(throttleConf.removeTimeLimit).thenReturn(None)

    Get("/") ~> throttledRoute ~> check {
      assert(status === StatusCodes.OK)
      assert(responseAs[String] === "done")
      verify(service, times(1)).doSomething()
    }
  }
}
