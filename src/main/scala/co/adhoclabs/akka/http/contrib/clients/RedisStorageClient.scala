package co.adhoclabs.akka.http.contrib.clients

import co.adhoclabs.akka.http.contrib.Config
import com.redis.RedisClientPool

class RedisStorageClient(val redis: RedisClientPool) extends StorageClient {
  private def cacheKey(key: String): String = Config.Redis.namespace + key

  override def getCount(key: String): Option[Int] = {
    val thing = cacheKey(key)
    redis.withClient(_.get(thing)).map(_.toInt)
  }

  override def incrementCount(key: String, expiration: Option[Int]): Unit = {
    val throttleKey = cacheKey(key)
    val newCount = redis.withClient(_.get(throttleKey)).fold(0)(_.toInt + 1)

    redis.withClient(_.setex(throttleKey, expiration.getOrElse(Config.Redis.defaultExpiration), newCount))
  }

  override def clearCount(key: String): Option[Long] = {
    redis.withClient(_.del(cacheKey(key)))
  }
}
