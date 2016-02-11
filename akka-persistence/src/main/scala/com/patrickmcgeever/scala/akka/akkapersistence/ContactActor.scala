package com.patrickmcgeever.scala.akka.akkapersistence

import akka.actor.{ActorRef, Props}
import akka.persistence.PersistentActor
import com.patrickmcgeever.scala.akka.akkapersistence.Messages._

object ContactActor {
  def props(receiver: ActorRef): Props = Props(new ContactActor(receiver))
}

case class ContactActorState(contacts: Map[String, Contact] = Map()) {
  def added(contact: Contact): ContactActorState = copy(contacts + (contact.name -> contact))

  def retrieveContact(name: String): Option[Contact] = contacts.get(name)
}

class ContactActor(receiver: ActorRef) extends PersistentActor {

  override def persistenceId = "contact-actor-id"

  var state = ContactActorState()

  def addToContacts(contact: Contact) = state = state.added(contact)

  override def receiveCommand: Receive = {
    case CreateContact(contact) =>
      persist(contact)(addToContacts)
      receiver ! ContactCreated

    case RetrieveContact(name) =>
      receiver ! state.retrieveContact(name)
  }

  override def receiveRecover: Receive = {
    case contact: Contact => addToContacts(contact)
  }
}
