package theming.config

import com.typesafe.config.ConfigFactory

trait ApplicationConfig {

  private val config = ConfigFactory.load()
  private val httpConfig = config.getConfig("http")
  val httpHost: String = httpConfig.getString("host")
  val httpPort: Int = httpConfig.getInt("port")

  private val dbConfig = config.getConfig("database")
  val databaseConfig: DatabaseConfig = DatabaseConfig(
    dbConfig.getString("url"),
    dbConfig.getString("user"),
    dbConfig.getString("password"),
    dbConfig.getString("driverClassName")
  )
}
