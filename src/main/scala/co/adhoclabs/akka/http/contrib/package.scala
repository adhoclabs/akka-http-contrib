package co.adhoclabs.akka.http

import co.adhoclabs.akka.http.contrib.clients.RedisStorageClient
import com.redis.RedisClientPool
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

package object contrib {

  /*
  * Holds the configuration necessary to decide whether a throttle should be engaged or not.
  * maxCalls is the number of calls that can be made within a time limit. (i.e: 10 calls/minute)
  * removeTimeLimit is the amount of time in which the throttle should be active after engaging.
  * (i.e: the throttle should automatically disengage after 6 hours)
  *
  */
  case class ThrottleConfiguration(maxCalls: Int, removeTimeLimit: Option[Int])

  object StorageClient extends Enumeration {
    val REDIS = Value(1, "redis")
  }

  trait Config {
    private val config = ConfigFactory.load()
    private val akkaHttpContribConfig = config.getConfig("akka.http.contrib")

    object Redis {
      private val redisConfig = akkaHttpContribConfig.getConfig("redis")
      private val s = redisConfig.getString("secret")
      val host = redisConfig.getString("host")
      val port = redisConfig.getInt("port")
      val db = redisConfig.getInt("db")
      val secret = if (s == "") None else Some(s)
      val namespace = redisConfig.getString("namespace")
      val defaultExpiration = redisConfig.getInt("default-expiration")
    }

    lazy val storageClient = StorageClient.withName(akkaHttpContribConfig.getString("storage-client")) match {
      case client if client == StorageClient.REDIS ⇒
        val redisClientPool: RedisClientPool = new RedisClientPool(
          host = Config.Redis.host,
          port = Config.Redis.port,
          database = Config.Redis.db,
          secret = Config.Redis.secret
        )
        new RedisStorageClient(redisClientPool)
    }
  }

  object Config extends Config

}
