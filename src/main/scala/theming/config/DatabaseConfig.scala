package theming.config

import java.sql.Driver

case class DatabaseConfig(url: String, user: String, password: String, driverClassName: String) {
  val driver: Driver = Class.forName(driverClassName).getDeclaredConstructor().newInstance().asInstanceOf[Driver]
}
