package com.pmcgeever.akkahttpfileupload

import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

object AkkaHttpFileUpload extends App {

  private val log = LoggerFactory.getLogger(AkkaHttpFileUpload.getClass)

  val config: Config = ConfigFactory.load()
  implicit val system = ActorSystem("AkkaHttpFileUpload", config)
  implicit val materializer = ActorMaterializer()

  Try {
    val applicationConfig = ApplicationConfig(config.getConfig("akka-http-file-upload"))
    val invocationService = system.actorOf(InvocationService.props(applicationConfig))
    val fileHandler = system.actorOf(Props(new FileHandler))
    val route = Api.route(applicationConfig, invocationService, fileHandler)
    bindRestService(applicationConfig, route)
    log.info("Server is listening to {}:{}", applicationConfig.interface, applicationConfig.port)
  } recover {
    case th =>
      log.error("There was an error launching the web server, exiting", th)
      system.terminate
      Await.result(system.whenTerminated, Duration(30, TimeUnit.SECONDS))
      System.exit(1)
  }

  private def bindRestService(config: ApplicationConfig, route: Route) = {
    val interface = config.interface
    val port = config.port
    Http()(system).bindAndHandle(
      route,
      interface,
      port)
  }
}
