package co.adhoclabs.akka.http.contrib.clients

trait StorageClient {
  def getCount(key: String): Option[Int]
  def incrementCount(key: String, expiration: Option[Int]): Unit
  def clearCount(key: String): Option[Long]
}
