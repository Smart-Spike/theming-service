package theming.config

import com.typesafe.config.ConfigFactory

trait ApplicationConfig {

  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  val httpHost = httpConfig.getString("host")
  val httpPort = httpConfig.getInt("port")

}
