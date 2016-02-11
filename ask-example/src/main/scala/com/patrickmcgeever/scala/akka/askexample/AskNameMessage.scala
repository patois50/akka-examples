package com.patrickmcgeever.scala.akka.askexample

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern
import akka.pattern.ask

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class AskNameMessage(name: String)

class TestActor extends Actor {
  override def receive: Receive = {
    case message: AskNameMessage => // respond to the "ask" request
      sender ! "Hi %s my name is Fred".format(message.name)
    case _ => println("that was unexpected")
  }
}

object AskTest extends App {

  //Create the system and actor
  val system = ActorSystem("AskTestSystem")
  val myActor = system.actorOf(Props[TestActor], name = "myActor")

  // (1) One way to ask an actor

  // Better practise to send asychronous messages and have receiver actors or send message back to the sender


//  val future = myActor ? AskNameMessage
//  val result = Await.result(future, (5 seconds)).asInstanceOf[String]
//  println(result)
//
//
//    // (2) this is a slightly different way to ask another actor
//  val future2: Future[String] = pattern.ask(myActor, AskNameMessage("Patrick"))(5 seconds).mapTo[String]
//  val result2 = Await.result(future2, 1 second)
//  println(result2)

  system.terminate()
}