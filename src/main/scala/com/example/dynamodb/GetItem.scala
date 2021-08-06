package com.example.dynamodb

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.dynamodb.AwsOp._
import akka.stream.alpakka.dynamodb.scaladsl.DynamoDb
import com.amazonaws.services.dynamodbv2.model.{ AttributeValue, DescribeTableRequest, GetItemRequest, KeySchemaElement, KeyType, TableDescription }
import com.example.dynamodb.Implicts.FutureOps

import scala.collection.mutable
import scala.concurrent.Future
import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

case class MetaData(name: String)
case class Location(latitude: Long, longitude: Long)
case class FormattedAddress(formattedAddress: String)
case class Event(
    summary: String,
    addressDescription: FormattedAddress,
    endTimestamp: Long,
    endUTCOffset: Long,
    externalEventId: String,
    externalEventSource: String,
    reminderMinutes: Long,
    startTimestamp: Long,
    startUTCOffset: Long,
    location: Location,
    metadata: MetaData)
case class CalendarEvent(userId: UUID, eventId: String, removed: Boolean, value: Event)

object CalendarEvent extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val metadataFormat = jsonFormat1(MetaData)
  implicit val locationFormat = jsonFormat2(Location)
  implicit val formattedAddressFormat = jsonFormat1(FormattedAddress)
  implicit val eventFormat = jsonFormat11(Event)
  implicit val calendarEventFormat = jsonFormat4(CalendarEvent)
}
object GetItem extends App {
  private val entityId = "calendar-events-c9568234-27dc-40e6-a248-dbdf473c28cd"
  private val userId = "bf91f8a9-b186-43f7-963d-c4724c0b73f3"
  private val tableName = "Calendar-Events"

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val selector = mutable.Map("userId" -> new AttributeValue(userId), "entityId" -> new AttributeValue(entityId))
  import collection.JavaConverters._
  val request = new GetItemRequest(tableName, selector.asJava)

  val getItem = DynamoDb.single(request)
  getItem
    .map { item =>
      val attributes = item.getItem.asScala
      attributes.mapValues(_.withN())

      s"""
       |${attributes.mkString("\n")}""".stripMargin
    }
    .show()

}
