package theming.config

import java.sql.Connection
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

class SchemaMigration(config: DatabaseConfig) extends LazyLogging {

  val masterChangeLogFile = "db/db.changelog-master.xml"

  private def connectionProperties = {
    val connectionProperties = new Properties()
    connectionProperties.put("user", config.user)
    connectionProperties.put("password", config.password)
    connectionProperties
  }

  private def createLiquibase(dbConnection: Connection, diffFilePath: String): Liquibase = {
    val database = DatabaseFactory.getInstance.findCorrectDatabaseImplementation(new JdbcConnection(dbConnection))
    new Liquibase(diffFilePath, new ClassLoaderResourceAccessor(), database)
  }

  def run(): Unit = {
    logger.info("Migrating database")
    val dbConnection = config.driver.connect(config.url, connectionProperties)
    val liquibase = createLiquibase(dbConnection, masterChangeLogFile)
    try {
      liquibase.update(null)
    } catch {
      case e: Throwable => throw e
    } finally {
      liquibase.forceReleaseLocks()
      dbConnection.rollback()
      dbConnection.close()
    }
  }

}
