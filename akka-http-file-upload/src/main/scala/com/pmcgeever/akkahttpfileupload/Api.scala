package com.pmcgeever.akkahttpfileupload

import java.nio.file.{Path, Paths}
import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO
import com.pmcgeever.akkahttpfileupload.Api.IDGenerator
import com.pmcgeever.akkahttpfileupload.InvocationService.{Invocation, Invoke}
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter}
import spray.json._

import scala.util.{Failure, Success}

object Api {
  type IDGenerator = () => String

  val uuidGenerator = () => UUID.randomUUID().toString

  def route(config: ApplicationConfig, invocationService: ActorRef)(implicit materializer: Materializer) = {
    val api = new Api(config, invocationService, uuidGenerator)
    api.file ~ api.hello
  }

  object InvocationProtocol extends DefaultJsonProtocol {
    implicit object DateTimeFormat extends RootJsonFormat[DateTime] {
      private val parserISO : DateTimeFormatter = ISODateTimeFormat.dateTime();

      override def write(obj: DateTime) = JsString(parserISO.print(obj))

      override def read(json: JsValue) : DateTime = json match {
        case JsString(s) => parserISO.parseDateTime(s)
        case _ => throw new DeserializationException("Could not parse dateTime")
      }
    }

    implicit object PathFormat extends RootJsonFormat[Path] {

      override def write(obj: Path) = JsString(obj.toString)

      override def read(json: JsValue) : Path = json match {
        case JsString(s) => Paths.get(s)
        case _ => throw new DeserializationException("Could parse path")
      }
    }

    implicit val invocationProtocol = jsonFormat7(Invocation)
  }

}

class Api(config: ApplicationConfig,
          invocationService: ActorRef,
          idGenerator: IDGenerator)(implicit val materializer: Materializer) extends Directives {
  import com.pmcgeever.akkahttpfileupload.Api.InvocationProtocol._

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
          val id = idGenerator()
          val filePath = Paths.get(config.dataDir).resolve(id).resolve(fileInfo.fileName)

          val sink = FileIO.toPath(filePath)
          val writeResult = fileStream.runWith(sink)
          onSuccess(writeResult) { result =>
            result.status match {
              case Success(_) =>
                val invocation = Invocation(id = idGenerator(), inputFile = filePath)
                invocationService ! Invoke(invocation)
                complete(invocation.toJson.toString)
              case Failure(th) => failWith(th)
            }
          }
      }
    }
  }
}
