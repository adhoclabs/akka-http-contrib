package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.RemoteAddress
import scredis.io.{Connection, NonBlockingConnection, TransactionEnabledConnection}

import scala.concurrent.{ExecutionContext, Future}

object RedisMetricStore {
  import scredis.commands._

  type RedisT = ConnectionCommands with ServerCommands with KeyCommands with StringCommands with HashCommands with ListCommands with SetCommands with SortedSetCommands with ScriptingCommands with HyperLogLogCommands with PubSubCommands with TransactionCommands
  trait RedisTT extends ConnectionCommands
    with ServerCommands
    with KeyCommands
    with StringCommands
    with HashCommands
    with ListCommands
    with SetCommands
    with SortedSetCommands
    with ScriptingCommands
    with HyperLogLogCommands
    with PubSubCommands
    with TransactionCommands
    with Connection
    with NonBlockingConnection
    with TransactionEnabledConnection
}

class RedisMetricStore(
    val redis: RedisMetricStore.RedisT, namespace: String = ""
)(implicit ec: ExecutionContext) extends MetricStore {
  override def keyForEndpoint(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): String =
    s"$namespace${super.keyForEndpoint(throttleEndpoint, remoteAddress, url)}"

  override def get(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Long] = {
    import scredis.serialization.Implicits.longReader
    redis.get(keyForEndpoint(throttleEndpoint, remoteAddress, url)).map(_.getOrElse(0))
  }

//  override def set(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String, count: Long): Future[Unit] =
//    redis.pSetEX(keyForEndpoint(throttleEndpoint, remoteAddress, url), count, throttleEndpoint.throttleDetails.window.toMillis)

  override def incr(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Unit] = {
    val key = keyForEndpoint(throttleEndpoint, remoteAddress, url)
    val ex = throttleEndpoint.throttleDetails.window.toMillis
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
