package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.{ HttpMethods, HttpRequest, Uri }
import co.adhoclabs.akka.http.contrib.stores.MetricStore
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar

import scala.concurrent.duration.Duration

class ThrottleSettingsTest
    extends FunSuite
    with MockitoSugar
    with BeforeAndAfter {

  private val getEndpoint = ThrottleEndpoint(Endpoint("GET", "/user/{id}"), Expiration(Duration.create("1h"), 10000))
  private val putEndpoint = ThrottleEndpoint(Endpoint("PUT", "/user/{id}"), Expiration(Duration.create("1h"), 10000))
  private val mockedStore = mock[MetricStore]

  object MockedRedisSettings extends MetricThrottleSettings {
    override def endpoints = getEndpoint :: putEndpoint :: Nil

    override def store = mockedStore
  }

  before {
    reset(mockedStore)
  }

  test("MetricThrottleSettings should be constructed from config") {
    val settings = MetricThrottleSettings.fromConfig

    assert(settings.store !== null)
  }

  test("MetricThrottleSettings should read the endpoints from the config") {
    val settings = MetricThrottleSettings.fromConfig
    val endpoints = getEndpoint :: putEndpoint :: Nil

    assert(settings.endpoints === endpoints)
  }

  test("MetricThrottleSettings shouldThrottle should return false if request uri does not match any configured endpoints") {
    val settings = MockedRedisSettings
    val req = HttpRequest(HttpMethods.GET, Uri("http://localhost/burner/someBurnerId/setting"))

    assert(settings.shouldThrottle(req) === false)
    verify(settings.store, times(0)).get(anyObject[ThrottleEndpoint], anyString())
  }

  test("MetricThrottleSettings shouldThrottle should return false if request uri is under throttle limited") {
    val settings = MockedRedisSettings
    val req = HttpRequest(HttpMethods.GET, Uri("http://localhost/user/someBurnerId"))
    when(settings.store.get(getEndpoint, "/user/someBurnerId")).thenReturn(10L)
    assert(settings.shouldThrottle(req) === false)
    verify(settings.store, times(1)).get(getEndpoint, "/user/someBurnerId")
  }

  test("MetricThrottleSettings onExecute should do nothing if request uri does not match any configured endpoints") {
    val settings = MockedRedisSettings
    val req = HttpRequest(HttpMethods.GET, Uri("http://localhost/burner/someBurnerId/setting"))

    settings.onExecute(req)

    verify(settings.store, times(0)).incr(anyObject[ThrottleEndpoint], anyString())
  }

  test("MetricThrottleSettings onExecute should return false if request uri is under throttle limited") {
    val settings = MockedRedisSettings
    val req = HttpRequest(HttpMethods.GET, Uri("http://localhost/user/someBurnerId"))
    when(settings.store.get(getEndpoint, "/user/someBurnerId")).thenReturn(10L)
    settings.onExecute(req)
    verify(settings.store, times(1)).incr(getEndpoint, "/user/someBurnerId")
  }
}
