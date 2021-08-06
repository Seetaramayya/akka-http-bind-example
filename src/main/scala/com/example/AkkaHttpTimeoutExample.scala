package com.example

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.directives.FutureDirectives._
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.directives.{ BasicDirectives, CodingDirectives }
import akka.http.scaladsl.server.{ Route, RouteConcatenation }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.example.Implicits.FutureExt
import com.example.actors.SlowResponseActor
import com.example.actors.SlowResponseActor.GetPing

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object AkkaHttpTimeoutExample extends App with RouteConcatenation with BasicDirectives with CodingDirectives {
  implicit val system = ActorSystem("seeta-actor-system")
  implicit val log = system.log
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher
  implicit val timeout = Timeout(5.seconds)
  private val actorRef = system.actorOf(SlowResponseActor.props)

  val route: Route = path("ping") {
    val future = (actorRef ? GetPing).mapTo[String]
    onComplete(future) {
      case Success(value)     => complete(value)
      case Failure(exception) => complete(StatusCodes.InternalServerError -> exception.getMessage)
    }
  }

  val bindAndHandleFuture: Future[ServerBinding] = Http().bindAndHandle(route, "127.0.0.1", 8080)
  bindAndHandleFuture.showMessage()

  sys.addShutdownHook {
    system.terminate().onComplete {
      case Success(terminated) => println(s"Terminated ${terminated}")
      case Failure(t)          => t.printStackTrace()
    }
  }

}
