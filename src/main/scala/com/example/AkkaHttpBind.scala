package com.example

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest, RequestEntity, StatusCodes }
import akka.http.scaladsl.model.StatusCodes.{ InternalServerError, OK }
import akka.http.scaladsl.server.directives.{ BasicDirectives, CodingDirectives }
import akka.http.scaladsl.server.directives.FutureDirectives._
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.{ Directive0, Route, RouteConcatenation }
import akka.stream.{ ActorMaterializer, ThrottleMode }
import akka.stream.scaladsl.{ Sink, Source }
import akka.pattern.{ ask, pipe }
import akka.util.{ ByteString, Timeout }
import com.example.Implicits.FutureExt
import com.example.actors.SlowResponseActor
import com.example.actors.SlowResponseActor.GetPing

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.util.{ Failure, Random, Success }

object DownStreamActor {
  val oneHour = 60 * 60 * 1000
  val oneMin = 60 * 1000
  val oneSecond = 1000

  def pingRespone(implicit ec: ExecutionContext): Future[String] = Future {
    Seeta.veryTimeConsuming
    "PONG"
  }
}

object AkkaHttpBind extends App with RouteConcatenation with BasicDirectives with CodingDirectives {
  implicit val system = ActorSystem("seeta-actor-system")
  implicit val log = system.log
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val isPrime: Int => Boolean = {
    case x if x <= 1 => false
    case 2           => true
    case number      => (2 until number).forall(i => number % i != 0)
  }

  val route: Route = path("ping") {
    implicit val timeout = Timeout(5.seconds)
    val actorRef = system.actorOf(SlowResponseActor.props)
    onComplete((actorRef ? GetPing).mapTo[String]) {
      case Success(value)     => complete(value)
      case Failure(exception) => complete(StatusCodes.InternalServerError -> exception.getMessage)
    }
  } ~ path("prime" / IntNumber) { number =>
    val response = if (isPrime(number)) s"Given $number is prime" else s"Given $number is not a prime"
    complete(OK -> response)
  }

  def decrypt(request: HttpRequest): Future[RequestEntity] = {
    request.entity.dataBytes.runFold(ByteString.empty)(_ ++ _).map { data =>
      system.log.info("Data is decrypted {}, {}", data.length, request.entity.contentType)
      HttpEntity(request.entity.contentType, data)
    }
  }

  def seetaDirective: Directive0 = mapInnerRoute { inner => ctx =>
    if (ctx.request.entity.isKnownEmpty()) {
      inner(ctx)
    } else {
      decrypt(ctx.request).flatMap { ent =>
        system.log.info("{} decrypted, size is {}", ctx.request.uri, ctx.request.entity.contentLengthOption)
        inner(ctx.withRequest(ctx.request.withEntity(ent)))
      }
    }
  }

  val routeExample: Route = pathPrefix("api") {
    pathPrefix("v1" / "s") {
      path("sessions") {
        val sessions =
          """
            |{
            |  "access_token": "c5760760-d3b8-48c4-90a9-d81b015b32a7",
            |  "creation_time": "2020-08-04T06:26:51.941Z",
            |  "user_id": "09969ced-2d42-42cc-ad7a-c612d7653ec8"
            |}
            |""".stripMargin
        complete(OK -> sessions)
      } ~ path("pois") {
        (decodeRequest & encodeResponse) {
          seetaDirective {
            extractRequestContext { ctx =>
              val futureData = ctx.request.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
              onComplete(futureData) {
                case Success(data) =>
                  system.log.info("data size is {}", data.length)
                  complete(OK)
                case Failure(ex) =>
                  system.log.error("failed to read data {}", ex.getMessage)
                  complete(InternalServerError)
              }
            }
          }
        }
      }
    }
  }

  private val host = "127.0.0.1"
  private val bindAndHandlePort = 8080
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

  bindFuture.showMessage()
  bindAndHandleFuture.showMessage()

  sys.addShutdownHook {
    system.terminate().onComplete {
      case Success(terminated) => println(s"Terminated ${terminated}")
      case Failure(t)          => t.printStackTrace()
    }
  }

}
