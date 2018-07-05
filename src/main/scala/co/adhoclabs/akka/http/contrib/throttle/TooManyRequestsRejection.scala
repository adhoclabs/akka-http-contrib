package co.adhoclabs.akka.http.contrib.throttle

import akka.http.scaladsl.server.Rejection

final case class TooManyRequestsRejection() extends Rejection