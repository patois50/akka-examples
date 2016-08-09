package com.patrickmcgeever.akka

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.DurationLong

object ActorMonitor extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = Timeout(2 seconds)

  val system = ActorSystem("ActorExampleOne")
  val pong = system.actorOf(Props[PongResponder], "pongResponder")
  val pongMonitor = system.actorOf(Props(classOf[Monitor], pong), "monitor")
  println("Sending ping")
  (pong ? "ping").onSuccess{
    case s: String => println(s"Received $s")
  }
  system.terminate
}

class Monitor(actor: ActorRef) extends Actor {
  context.watch(actor)

  override def receive: Actor.Receive = {
    case Terminated(ar) => println(s"The ${ar.path.name} actor has terminated")
  }
}

class PongResponder extends Actor {
  override def receive: Receive = {
    case "ping" =>
      sender ! "pong"
      self ! PoisonPill
  }
}
