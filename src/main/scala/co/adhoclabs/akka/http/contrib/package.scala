package co.adhoclabs.akka.http

package object contrib {
  object StorageClient extends Enumeration {
    val REDIS = Value(1, "redis")
  }
}
