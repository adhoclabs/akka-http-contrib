package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.model.HttpRequest
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ ExecutionContext, Future }

trait MetricThrottleSettings extends ThrottleSettings with StrictLogging {
  def store: MetricStore
  def endpoints: List[ThrottleEndpoint]

  protected def findAndRun[T](
      request: HttpRequest,
      left: => Future[T]
  )(right: (ThrottleEndpoint) ⇒ Future[T]): Future[T] =
    Future
      .sequence(endpoints.map(te ⇒ te.endpoint.matches(request).map(m ⇒ if (m) Some(te) else None)))
      .map(_.flatten.headOption)
      .flatMap(_.fold(left)(right))

  override def shouldThrottle(request: HttpRequest): Future[Boolean] = {
    findAndRun(request, {
      logger.debug(s"not checking cache for $request")
      Future(false)
    }) { te ⇒
      logger.info(s"checking cache for $request")
      store
        .get(te, request.uri.path.toString()) // TODO - use full request instead of uri
        .map { count =>
          logger.debug(
            s"checking current access count $count against the limit: ${te.throttleDetails.allowedCalls}"
          )
          count >= te.throttleDetails.allowedCalls
        }
    }
  }

  override def onExecute(request: HttpRequest): Future[Unit] =
    findAndRun(request, Future(())) { te ⇒
      store.incr(te, request.uri.path.toString())
    }
}

object MetricThrottleSettings {
  def fromConfig(implicit ec: ExecutionContext): MetricThrottleSettings =
    new ConfigMetricThrottleSettings {
      implicit override val executor: ExecutionContext = ec
    }

  def fromConfig(cfg: Config)(implicit ec: ExecutionContext): MetricThrottleSettings =
    new ConfigMetricThrottleSettings {
      override lazy val config: Config                 = cfg
      implicit override val executor: ExecutionContext = ec
    }
}
