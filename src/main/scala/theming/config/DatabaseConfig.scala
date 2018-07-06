package theming.config

import java.sql.Driver

import slick.jdbc.JdbcBackend._

case class DatabaseConfig(url: String, user: String, password: String, driverClassName: String) {
  val driver: Driver = Class.forName(driverClassName).getDeclaredConstructor().newInstance().asInstanceOf[Driver]
  val database: Database = Database.forURL(url, user, password, driver = driverClassName)
}
