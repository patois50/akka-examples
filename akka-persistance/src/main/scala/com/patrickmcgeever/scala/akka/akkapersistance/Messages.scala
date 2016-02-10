package com.patrickmcgeever.scala.akka.akkapersistance

object Messages {
  sealed trait ContactMessage
  case class CreateContact(contact: Contact) extends ContactMessage
  object AddEmail extends ContactMessage

  object Success
  object Failure
}
