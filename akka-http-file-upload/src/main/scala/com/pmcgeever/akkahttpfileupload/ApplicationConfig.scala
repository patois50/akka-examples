package com.pmcgeever.akkahttpfileupload

import com.typesafe.config.Config

object ApplicationConfig {
  def apply(config: Config): ApplicationConfig =
    ApplicationConfig(
      port = config.getInt("port"),
      interface = config.getString("interface"),
      dataDir = config.getString("data-dir"))
}

case class ApplicationConfig(port: Int, interface: String, dataDir: String)
