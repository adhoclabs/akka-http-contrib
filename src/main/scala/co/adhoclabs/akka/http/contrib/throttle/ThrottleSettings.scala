package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpRequest
import co.adhoclabs.akka.http.contrib.StorageClient
import co.adhoclabs.akka.http.contrib.stores.{ MetricStore, RedisMetricStore }
import com.redis.RedisClientPool
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

trait ThrottleSettings {
  def shouldThrottle(request: HttpRequest): Boolean

  def onExecute(request: HttpRequest): Unit
}

object MetricThrottleSettings {
  private val config = ConfigFactory.load()
  private val redisConfig = config.getConfig("akka.http.contrib.redis")

  def fromConfig: MetricThrottleSettings = {
    new MetricThrottleSettings {
      override def store: MetricStore = {
        config.getString("akka.http.contrib.storage-client") match {
          case client if client == StorageClient.REDIS.toString ⇒
            val s = redisConfig.getString("secret")
            val secret = if (s == "") None else Some(s)
            val redisClientPool: RedisClientPool = new RedisClientPool(
              host = redisConfig.getString("host"),
              port = redisConfig.getInt("port"),
              database = redisConfig.getInt("db"),
              secret = secret
            )
            new RedisMetricStore(redisClientPool)
        }
      }

      override def endpoints: List[ThrottleEndpoint] = {
        if (config.getBoolean("akka.http.contrib.throttle-enabled")) {
          config.getConfigList("akka.http.contrib.endpoints").asScala.toList.map { c ⇒
            val method = c.getString("method")
            val uri = c.getString("uri")
            val duration = Duration.create(c.getString("window"))
            val allowedCalls = c.getInt("allowedCalls")
            ThrottleEndpoint(Endpoint(method, uri), Expiration(duration, allowedCalls))
          }
        } else {
          Nil
        }
      }
    }
  }
}

trait MetricThrottleSettings extends ThrottleSettings {
  def store: MetricStore

  def endpoints: List[ThrottleEndpoint]

  override def shouldThrottle(request: HttpRequest): Boolean = endpoints
    .filter(_.endpoint.matches(request))
    .exists(te ⇒ store.get(te, request.uri.path.toString()) >= te.expiration.allowedCalls)

  override def onExecute(request: HttpRequest): Unit = endpoints
    .filter(_.endpoint.matches(request))
    .foreach(te ⇒ store.incr(te, request.uri.path.toString()))
}