package com.pmcgeever.akkahttpfileupload

import java.nio.file.{Paths, Path}

import akka.actor.{Props, Actor, ActorLogging}
import com.pmcgeever.akkahttpfileupload.InvocationService.{TimeProvider, InvocationId, Invocation, Invoke}
import org.joda.time.{DateTimeZone, DateTime}
import scala.sys.process.Process

object InvocationService {
  type InvocationId = String
  type TimeProvider = () => DateTime

  val nowDateTime = () => new DateTime(DateTimeZone.UTC)

  case class Invocation(id: InvocationId,
                        inputFile: Path,
                        startedAt: Option[DateTime] = None,
                        command: Option[String] = None,
                        logFile: Option[Path] = None,
                        endedAt: Option[DateTime] = None,
                        outputFile: Option[Path] = None)
  case class Invoke(invocation: Invocation)

  def props(config: ApplicationConfig) = Props(new InvocationService(config, nowDateTime))
}

class InvocationService(config: ApplicationConfig, time: TimeProvider) extends Actor with ActorLogging {

  var invocations = Map.empty[InvocationId, Invocation]

  override def receive: Receive = {
    case Invoke(invocation) =>
      val logPath = Paths.get(config.dataDir).resolve(invocation.id).resolve("log.txt")
      val command = s"ls -alrh > ${logPath.toAbsolutePath.toString}"
      val now = time()
      val newInvocation = invocation.copy(
        startedAt = Some(now),
        command = Some(command),
        logFile = Some(logPath)
      )
      invocations = invocations + (invocation.id -> invocation)
      Process(command).run
  }
}
