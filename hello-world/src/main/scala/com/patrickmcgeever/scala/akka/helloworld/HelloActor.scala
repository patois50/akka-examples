package com.patrickmcgeever.akka.example

import akka.actor.{Actor, ActorSystem, Props}

/**
 * Created by pmcgeever on 12/01/16.
 */
class HelloActor extends Actor {
  override def receive: Receive = {
    case "hello" => println("hello back at you")
    case _ => println("huh?")
  }
}

object Main extends App {
  val system = ActorSystem("HelloSystem")

  // create an actor with the "actorOf(Props[TYPE])" syntax
  val helloActor = system.actorOf(Props[HelloActor], name = "helloactor")
  helloActor ! "hello"
  helloActor ! "buenos dias"
}
