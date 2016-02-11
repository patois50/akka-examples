package com.patrickmcgeever.scala.akka.akkapersistence

import akka.actor.{Props, ActorRef, Actor}
import com.patrickmcgeever.scala.akka.akkapersistence.Messages._

object ContactActor {
  def props(receiver: ActorRef): Props = Props(new ContactActor(receiver))
  var contacts: Map[String, Contact] = Map()
}

class ContactActor(receiver: ActorRef) extends Actor {

  import com.patrickmcgeever.scala.akka.akkapersistence.ContactActor._

  def createContact(contact: Contact) = {
    contacts = contacts + (contact.name -> contact)
  }

  override def receive = {
    case CreateContact(contact) =>
      createContact(contact)
      receiver ! ContactCreated
    case RetrieveContact(name) =>
      val contact = contacts.get(name)
      receiver ! (if(contact.nonEmpty) ContactFound(contact.get) else ContactNotFound)
  }
}
