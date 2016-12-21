package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.RemoteAddress
import akka.util.ByteString
import redis.{ ByteStringDeserializer, RedisClient }

import scala.concurrent.{ ExecutionContext, Future }

class RedisMetricStore(
    val redisClient: RedisClient, namespace: String = ""
)(implicit ec: ExecutionContext) extends MetricStore {

  override def keyForEndpoint(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): String =
    s"$namespace${super.keyForEndpoint(throttleEndpoint, remoteAddress, url)}"

  override def get(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Long] = {

    implicit object ByteString extends ByteStringDeserializer[Long] {
      def deserialize(bs: ByteString): Long = bs.utf8String.toLong
    }

    val map: Future[Long] = redisClient.get[Long](keyForEndpoint(throttleEndpoint, remoteAddress, url)).map(_.getOrElse(0))
    map
  }

  override def incr(throttleEndpoint: ThrottleEndpoint, remoteAddress: RemoteAddress, url: String): Future[Unit] = {
    val key = keyForEndpoint(throttleEndpoint, remoteAddress, url)
    val ex = throttleEndpoint.throttleDetails.window.toMillis
    for {
      e ← redisClient.exists(key)
      v ← if (e) redisClient.incr(key) else redisClient.setex(key, 1L, ex).map(_ ⇒ 1L)
      _ ← throttleEndpoint.throttleDetails.throttlePeriod
        .filter(_ ⇒ v >= throttleEndpoint.throttleDetails.allowedCalls)
        .map(p ⇒ redisClient.expire(key, p.toSeconds))
        .getOrElse(Future(false))
    } yield ()
  }
}
