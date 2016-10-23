package com.pmcgeever.akkahttpfileupload

import java.nio.file.{Path, Paths}
import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RouteConcatenation._
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.pmcgeever.akkahttpfileupload.Api.IDGenerator
import com.pmcgeever.akkahttpfileupload.FileHandler._
import com.pmcgeever.akkahttpfileupload.InvocationService.{Invocation, Invoke}
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormatter, ISODateTimeFormat}
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object Api {
  type IDGenerator = () => String

  val uuidGenerator = () => UUID.randomUUID().toString

  def route(config: ApplicationConfig, invocationService: ActorRef, fileHandler: ActorRef)(implicit materializer: Materializer) = {
    val api = new Api(config, invocationService, fileHandler, uuidGenerator)
    api.file ~ api.hello
  }

  object InvocationProtocol extends DefaultJsonProtocol {

    implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
      private val parserISO: DateTimeFormatter = ISODateTimeFormat.dateTime();

      override def write(obj: DateTime) = JsString(parserISO.print(obj))

      override def read(json: JsValue): DateTime = json match {
        case JsString(s) => parserISO.parseDateTime(s)
        case _ => throw new DeserializationException("Could not parse dateTime")
      }
    }

    implicit object PathFormat extends RootJsonFormat[Path] {

      override def write(obj: Path) = JsString(obj.toString)

      override def read(json: JsValue): Path = json match {
        case JsString(s) => Paths.get(s)
        case _ => throw new DeserializationException("Could parse path")
      }
    }

    implicit val invocationProtocol = jsonFormat7(Invocation)
  }

}

class Api(config: ApplicationConfig,
          invocationService: ActorRef,
          fileHandler: ActorRef,
          id: IDGenerator)(implicit val materializer: Materializer) extends Directives {

  import com.pmcgeever.akkahttpfileupload.Api.InvocationProtocol._
  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val fileSaveTimeout = Timeout(config.fileSaveTimeout)

  val hello = path("hello" / Segment) {
    name =>
      get {
        complete(s"Hello $name")
      }
  }

  val file = path("fileUpload") {
    post {
      fileUpload("input") {
        case (fileInfo, fileStream) =>
          val invocationId = id()
          val fileDestination = s"${config.dataDir.toString}/$invocationId"
          val fut = fileHandler.ask(HandleFile(fileDestination, fileInfo, fileStream)) map {
            case FileSaved(filePath) =>
              val invocation = Invocation(invocationId, filePath)
              invocationService ! Invoke(invocation)
              complete(invocation.toJson.toString)
            case FileError(msg) =>
              complete(InternalServerError, msg)
          }
          Await.result(fut, 10 seconds)
      }
    }
  }
}
