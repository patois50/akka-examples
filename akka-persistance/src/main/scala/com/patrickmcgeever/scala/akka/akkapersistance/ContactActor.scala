package com.patrickmcgeever.scala.akka.akkapersistance

import akka.actor.Actor
import com.patrickmcgeever.scala.akka.akkapersistance.Messages.{Success, CreateContact}

object ContactActor {
  var contacts: Map[String, Contact] = Map()
}

class ContactActor extends Actor {

  import com.patrickmcgeever.scala.akka.akkapersistance.ContactActor._

  def createContact(contact: Contact) = {
    contacts = contacts + (contact.name -> contact)
  }

  override def receive = {
    case CreateContact(contact) =>
      createContact(contact)
      sender ! Success
  }
}
