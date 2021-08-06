package com.example.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.example.actors.SlowResponseActor.GetPing

object SlowResponseActor {
  case object GetPing
  def props: Props = Props(new SlowResponseActor)
}
class SlowResponseActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case GetPing => {
      (100000 to 201000).filter(isPrime).last
      sender() ! "PONG"
    }
  }

  def isPrime: Int => Boolean = {
    case x if x <= 1 => false
    case 2           => true
    case number      => (2 until number).forall(i => number % i != 0)
  }
}
