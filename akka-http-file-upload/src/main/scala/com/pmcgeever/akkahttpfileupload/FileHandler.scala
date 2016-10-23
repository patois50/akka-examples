package com.pmcgeever.akkahttpfileupload

import java.nio.file.{Files, Path, Paths}

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Source}
import akka.util.ByteString
import com.pmcgeever.akkahttpfileupload.FileHandler.{FileError, FileSaved, HandleFile}

import scala.util.Try

object FileHandler {

  case class HandleFile(fileDestination: String, fileInfo: FileInfo, fileStream: Source[ByteString, Any])

  case class FileSaved(filePath: Path)

  case class FileError(msg: String)

}

class FileHandler(implicit val materializer: ActorMaterializer) extends Actor with ActorLogging {

  implicit val executionContext = context.system.dispatchers.lookup("akka-http-file-upload.file-dispatcher")

  override def receive: Receive = {
    case HandleFile(fileDestination, fileInfo, fileStream) =>
      val originalSender = sender()
      Try {
        val invocationDir = Paths.get(fileDestination)
        Files.createDirectories(invocationDir)
        val filePath = invocationDir.resolve(fileInfo.fileName)
        val sink = FileIO.toPath(filePath)
        (filePath, sink)
      } map {
        case (filePath, sink) =>
          fileStream.runWith(sink) map {
            _ => originalSender ! FileSaved(filePath)
          } recover {
            case th =>
              val msg = s"Error saving file $fileDestination"
              log.error(th, msg)
              originalSender ! FileError(msg)
          }
      } recover {
        case th =>
          val msg = s"Error resolving destination dir $fileDestination"
          log.error(th, msg)
          originalSender ! FileError(msg)
      }
  }
}
