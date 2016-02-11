package com.patrickmcgeever.scala.akka.akkapersistance

object Messages {
  case class CreateContact(contact: Contact)
  object ContactCreated
  case class AddEmail(name: String, email: String)
  object EmailAddedToContact
  case class RetrieveContact(name: String)
  case class ContactFound(contact: Contact)
  object ContactNotFound

  object Success
  object Failure
}
