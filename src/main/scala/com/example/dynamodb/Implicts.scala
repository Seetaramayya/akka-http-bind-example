package com.example.dynamodb

import com.amazonaws.services.dynamodbv2.model.TableDescription

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object Implicts {
  implicit class FutureOps[T](future: Future[T]) {
    def show()(implicit ec: ExecutionContext): Unit = future onComplete {
      case Success(value)     => println(value)
      case Failure(exception) => exception.printStackTrace()
    }

  }
}
