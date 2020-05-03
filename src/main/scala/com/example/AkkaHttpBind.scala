package com.example

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes.{ InternalServerError, OK }
import akka.http.scaladsl.server.directives.FutureDirectives._
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.{ Route, RouteConcatenation }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Sink, Source }

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object AkkaHttpBind extends App with RouteConcatenation {
  implicit val system = ActorSystem("seeta-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val isPrime: Int => Boolean = {
    case x if x <= 1 => false
    case 2           => true
    case number      => (2 until number).forall(i => number % i != 0)
  }

  val route: Route = path("ping") {
    complete("pong")
  } ~ path("prime" / IntNumber) { number =>
    val response = if (isPrime(number)) s"Given $number is prime" else s"Given $number is not a prime"
    complete(OK -> response)
  }

  private val host = "127.0.0.1"
  private val bindAndHandlePort = 8585
  private val bindPort = 8686
  system.log.info("Starting servers on {} and {} ports", bindAndHandlePort, bindPort)

  val bindAndHandleFuture: Future[ServerBinding] = Http().bindAndHandle(route, host, bindAndHandlePort)
  val bindFuture: Future[ServerBinding] = Http()
    .bind(host, bindPort)
    .zip(Source.cycle(() => (1 to Integer.MAX_VALUE).iterator))
    .to(Sink.foreach {
      case (conn, i) =>
        system.log.info("Connection '{}' '{}'", i, conn.remoteAddress)
        conn.handleWith(route)
    })
    .run()

  implicit class FutureExt(future: Future[ServerBinding]) {
    def showMessage(): Unit = future.onComplete {
      case Success(value)     => system.log.info(s"BindAndHandle bound to '${value.localAddress}'")
      case Failure(exception) => system.log.error("Binding failed", exception)
    }
  }

  bindFuture.showMessage()
  bindAndHandleFuture.showMessage()

  sys.addShutdownHook {
    system.terminate().onComplete {
      case Success(terminated) => println(s"Terminated ${terminated}")
      case Failure(t)          => t.printStackTrace()
    }
  }

}
