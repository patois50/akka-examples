package com.patrickmcgeever.scala.akka.akkapersistence

object Messages {
  case class CreateContact(contact: Contact)
  object ContactCreated
  case class AddEmail(name: String, email: String)
  case class RetrieveContact(name: String)

  object Success
  object Failure
}
