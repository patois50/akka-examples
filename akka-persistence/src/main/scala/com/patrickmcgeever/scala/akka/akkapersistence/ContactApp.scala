package com.patrickmcgeever.scala.akka.akkapersistence

import akka.actor.{Actor, ActorSystem, Props}
import com.patrickmcgeever.scala.akka.akkapersistence.Messages.{ContactCreated, ContactFound, CreateContact, RetrieveContact}

import scala.io.StdIn

object ContactApp extends App {

  println("Usage:")
  println("n = new contact")
  println("e = add email to existing contact")
  println("r = retrieve contact")
  println("exit = exit app")
  println

  val system = ActorSystem("ContactApp")
  val receiverActor = system.actorOf(Props[Receiver], "receiver")
  val contactActor = system.actorOf(ContactActor.props(receiverActor), "contacts")

  var in = ""
  do {
    println("Select operation:")
    print("> ")
    in = StdIn.readLine()
    in match {
      case "n" => newContact()
      //      case "e" => addEmail()
      case "r" => retrieveContact()
      case "exit" => println("Exiting")
      case _ => println("Invalid selection")
    }
  } while (in != "exit")

  system.terminate()

  def newContact() = {
    println("Enter contact name:")
    print("> ")
    val name = StdIn.readLine()

    println("Enter contact telephone number:")
    print("> ")
    val telNo = StdIn.readLine()

    println("Enter contact email:")
    print("> ")
    val email = StdIn.readLine()

    val contact = Contact(name, Option(telNo), Option(email))

    contactActor ! CreateContact(contact)
  }

  def retrieveContact() = {
    println("Enter contact name:")
    print("> ")
    val name = StdIn.readLine()

    contactActor ! RetrieveContact(name)
  }

  class Receiver extends Actor {
    override def receive: Receive = {
      case ContactCreated => println("Contact created")
      case ContactFound(contact) => {
        val name = contact.name
        val telNo = if(contact.telNo.nonEmpty) contact.telNo.get else ""
        val email = if(contact.email.nonEmpty) contact.email.get else ""
        println(s"Name: $name")
        println(s"Telephone: $telNo")
        println(s"Email: $email")
      }
    }
  }
}
