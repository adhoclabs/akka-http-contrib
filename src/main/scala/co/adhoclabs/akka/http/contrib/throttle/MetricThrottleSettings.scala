package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpRequest

import scala.concurrent.{ ExecutionContext, Future }

trait MetricThrottleSettings extends ThrottleSettings {
  def store: MetricStore
  def endpoints: List[ThrottleEndpoint]

  protected def findAndRun[T](
    request: HttpRequest,
    left: Future[T]
  )(right: (ThrottleEndpoint) ⇒ Future[T]): Future[T] =
    Future.sequence(endpoints.map(te ⇒ te.endpoint.matches(request).map(m ⇒ if (m) Some(te) else None)))
      .map(_.flatten.headOption)
      .flatMap(_.fold(left)(right))

  override def shouldThrottle(request: HttpRequest): Future[Boolean] = findAndRun(request, Future(false)) { te ⇒
    store.get(te, request.uri.path.toString()).map(_ >= te.throttleDetails.allowedCalls)
  }

  override def onExecute(request: HttpRequest): Future[Unit] = findAndRun(request, Future(())) { te ⇒
    store.incr(te, request.uri.path.toString())
  }
}

object MetricThrottleSettings {
  def fromConfig(implicit ec: ExecutionContext): MetricThrottleSettings = new ConfigMetricThrottleSettings {
    override implicit val executor: ExecutionContext = ec
  }
}
