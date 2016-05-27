package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import co.adhoclabs.akka.http.contrib.ThrottleConfiguration
import co.adhoclabs.akka.http.contrib.clients.StorageClient

case class TooManyRequests(errorCode: Int, message: String) extends Rejection

trait ThrottleDirective extends Directives {
  implicit def throttleConf: ThrottleConfiguration
  implicit def storageClient: StorageClient

  private def isUnderLimit(
    client: StorageClient,
    key: String,
    conf: ThrottleConfiguration
  ) = client.getCount(key).exists(_ <= conf.maxCalls)

  /*
  * Given a key and a configuration, it applies a function f
  * to decide whether the throttle should engage or not.
  *
  * It requires a function g to transform the throttle metadata to a json string
  * in order to save the metadata into the storage and a json formatter to
  * read the string from the storage and transform it back into a Scala object.
  *
  */
  def throttle: Directive0 = extractRequest.flatMap { req ⇒
    val key = (req.method.value + req.uri.path.toString()).hashCode.toString
    if (isUnderLimit(storageClient, key, throttleConf)) Directive.Empty else complete(StatusCodes.TooManyRequests)
  }
}
