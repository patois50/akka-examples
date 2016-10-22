package com.pmcgeever.akkahttpfileupload

import java.nio.file.Paths

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.RouteConcatenation._
import akka.stream.Materializer
import akka.stream.scaladsl.FileIO

import scala.util.{Failure, Success}

object Api {
  def route(implicit materializer: Materializer) = {
    val api = new Api()
    api.file ~ api.hello
  }
}

class Api()(implicit val materializer: Materializer) extends Directives {

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
          val filePath = Paths.get("/tmp") resolve fileInfo.fileName
          val sink = FileIO.toPath(filePath)
          val writeResult = fileStream.runWith(sink)
          onSuccess(writeResult) { result =>
            result.status match {
              case Success(_) => complete(s"Successfully written ${result.count} bytes to ${filePath}")
              case Failure(th) => failWith(th)
            }
          }
      }
    }
  }
}
