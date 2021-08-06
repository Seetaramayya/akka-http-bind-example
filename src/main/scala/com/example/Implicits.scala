package com.example

import akka.event.LoggingAdapter
import akka.http.scaladsl.Http.ServerBinding

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Implicits {
  implicit class FutureExt(future: Future[ServerBinding]) {
    def showMessage()(implicit ec: ExecutionContext, log: LoggingAdapter): Unit = future.onComplete {
      case Success(value)     => log.info(s"BindAndHandle bound to '${value.localAddress}'")
      case Failure(exception) => log.error("Binding failed", exception)
    }
  }
}
