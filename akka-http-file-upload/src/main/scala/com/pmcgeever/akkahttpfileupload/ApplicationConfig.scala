package com.pmcgeever.akkahttpfileupload

import com.typesafe.config.Config

import scala.concurrent.duration.{FiniteDuration, Duration}

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig =
    ApplicationConfig(
      port = config.getInt("port"),
      interface = config.getString("interface"),
      dataDir = config.getString("data-dir"),
      fileSaveTimeout = Duration.create(config.getString("file-save-timeout")).asInstanceOf[FiniteDuration])
}

case class ApplicationConfig(port: Int, interface: String, dataDir: String, fileSaveTimeout: FiniteDuration)
