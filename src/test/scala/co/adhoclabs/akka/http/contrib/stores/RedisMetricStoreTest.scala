package co.adhoclabs.akka.http.contrib.stores

import co.adhoclabs.akka.http.contrib.throttle.{ Endpoint, Expiration, ThrottleEndpoint }
import com.redis.{ RedisClient, RedisClientPool }
import org.mockito.ArgumentCaptor
import org.scalatest.{ BeforeAndAfter, FunSuite }
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._

import scala.concurrent.duration.Duration

class RedisMetricStoreTest extends FunSuite
    with MockitoSugar
    with BeforeAndAfter {

  private val redisClientPoolMock = mock[RedisClientPool]
  private val redisClientMock = mock[RedisClient]
  private val store = new RedisMetricStore(redisClientPoolMock)
  private val getEndpoint = ThrottleEndpoint(Endpoint("GET", "/user/{id}"), Expiration(Duration.create("1h"), 10000))
  private val key = "sWgFkJ4eOcXztXPxjNOZkQ=="

  before {
    reset(redisClientPoolMock, redisClientMock)
  }

  test("keyForEndpoint should return a md5 hash") {
    val calculatedHash = store.keyForEndpoint(getEndpoint, "/user/someUserId")

    assert(calculatedHash === key)
  }

  test("get should return 0 for a non existent key") {
    val captor = ArgumentCaptor.forClass(classOf[RedisClient ⇒ Long])
    when(redisClientMock.get(key)).thenReturn(None)
    when(redisClientPoolMock.withClient(any())).thenReturn(None)
    assert(store.get(getEndpoint, "/user/someUserId") === 0L)
    verify(redisClientPoolMock, times(1)).withClient(captor.capture())
    captor.getValue.apply(redisClientMock)
    verify(redisClientMock, times(1)).get(key)
  }

  test("get should return the correct count for an existent key") {
    val captor = ArgumentCaptor.forClass(classOf[RedisClient ⇒ Long])
    when(redisClientMock.get(key)).thenReturn(Option("2"))
    when(redisClientPoolMock.withClient(any())).thenReturn(Option("2"))
    assert(store.get(getEndpoint, "/user/someUserId") === 2L)
    verify(redisClientPoolMock, times(1)).withClient(captor.capture())
    captor.getValue.apply(redisClientMock)
    verify(redisClientMock, times(1)).get(key)
  }

  test("set should set count to zero and set a new expiration date") {
    val key = "sWgFkJ4eOcXztXPxjNOZkQ=="
    val captor = ArgumentCaptor.forClass(classOf[RedisClient ⇒ Boolean])
    val expiration = ((System.currentTimeMillis() + getEndpoint.expiration.window.toMillis) / 1000).toInt
    when(redisClientMock.setex(key, expiration, 0L)).thenReturn(true)
    store.set(getEndpoint, "/user/someUserId", 0L)
    verify(redisClientPoolMock, times(1)).withClient(captor.capture())
    captor.getValue.apply(redisClientMock)
    verify(redisClientMock, times(1)).setex(key, expiration, 0L)
  }

  test("incr should set count to one and set a new expiration date for a non existent key") {
    val key = "sWgFkJ4eOcXztXPxjNOZkQ=="
    val captor = ArgumentCaptor.forClass(classOf[RedisClient ⇒ Boolean])
    val expiration = ((System.currentTimeMillis() + getEndpoint.expiration.window.toMillis) / 1000).toInt
    when(redisClientMock.get(key)).thenReturn(None)
    when(redisClientPoolMock.withClient(any())).thenReturn(None)
    when(redisClientMock.setex(key, expiration, 0)).thenReturn(true)
    store.incr(getEndpoint, "/user/someUserId")
    verify(redisClientPoolMock, times(2)).withClient(captor.capture())
    captor.getValue.apply(redisClientMock)
    verify(redisClientMock, times(1)).setex(key, expiration, 1)
    verify(redisClientMock, times(0)).incr(key)
  }

  test("incr should increment the count by one for an existent key") {
    val key = "sWgFkJ4eOcXztXPxjNOZkQ=="
    val captor = ArgumentCaptor.forClass(classOf[RedisClient ⇒ Boolean])
    val expiration = ((System.currentTimeMillis() + getEndpoint.expiration.window.toMillis) / 1000).toInt
    when(redisClientMock.get(key)).thenReturn(Option("2"))
    when(redisClientPoolMock.withClient(any())).thenReturn(Option("2"))
    when(redisClientMock.setex(key, expiration, 0)).thenReturn(true)
    when(redisClientMock.incr(key)).thenReturn(Option(3L))
    store.incr(getEndpoint, "/user/someUserId")
    verify(redisClientPoolMock, times(2)).withClient(captor.capture())
    captor.getValue.apply(redisClientMock)
    verify(redisClientMock, times(0)).setex(key, expiration, 0)
    verify(redisClientMock, times(1)).incr(key)
  }
}
