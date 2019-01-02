package co.adhoclabs.akka.http.contrib.throttle

import scredis.io.NonBlockingConnection
import scredis.protocol.Request

import scala.concurrent.{ ExecutionContext, Future }

object RedisMetricStore {
  import scredis.commands._

  type RedisT =
    NonBlockingConnection with KeyCommands with StringCommands

  trait RedisTT extends NonBlockingConnection with StringCommands with KeyCommands {
    override def send[A](request: Request[A]): Future[A] = request.future
  }
}

// used methods of RedisT: StringCommands#get, pSetEX & Keycommands#pExpire

class RedisMetricStore(
    val redis: RedisMetricStore.RedisT,
    namespace: String = ""
)(implicit ec: ExecutionContext)
    extends MetricStore {
  override def keyForEndpoint(throttleEndpoint: ThrottleEndpoint, url: String): String =
    s"$namespace${super.keyForEndpoint(throttleEndpoint, url)}"

  override def get(throttleEndpoint: ThrottleEndpoint, url: String): Future[Long] = {
    import scredis.serialization.Implicits.longReader
    redis.get(keyForEndpoint(throttleEndpoint, url)).map(_.getOrElse(0))
  }

  override def set(throttleEndpoint: ThrottleEndpoint, url: String, count: Long): Future[Unit] =
    redis.pSetEX(keyForEndpoint(throttleEndpoint, url),
                 count,
                 throttleEndpoint.throttleDetails.window.toMillis)

  override def incr(throttleEndpoint: ThrottleEndpoint, url: String): Future[Unit] = {
    val key = keyForEndpoint(throttleEndpoint, url)
    val ex  = throttleEndpoint.throttleDetails.window.toMillis
    for {
      e ← redis.exists(key)
      v ← if (e) redis.incr(key) else redis.pSetEX(key, 1L, ex).map(_ ⇒ 1L)
      _ ← throttleEndpoint.throttleDetails.throttlePeriod
        .filter(_ ⇒ v >= throttleEndpoint.throttleDetails.allowedCalls)
        .map(p ⇒ redis.pExpire(key, p.toMillis))
        .getOrElse(Future(false))
    } yield ()
  }
}
