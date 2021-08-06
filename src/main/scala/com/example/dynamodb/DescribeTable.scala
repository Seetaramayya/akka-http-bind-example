package com.example.dynamodb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.scaladsl.DynamoDb
import com.amazonaws.services.dynamodbv2.model.{ DescribeTableRequest, TableDescription }
import com.example.dynamodb.Implicts.FutureOps

import scala.concurrent.Future

object DescribeTable extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  val table: Future[TableDescription] = DynamoDb.single(new DescribeTableRequest("Calendar-Events")).map(_.getTable)

  table
    .map { description =>
      s"""Table Name: ${description.getTableName}
       |Table Status: ${description.getTableStatus}
       |Table Item count: ${description.getItemCount}""".stripMargin
    }
    .show()

}
