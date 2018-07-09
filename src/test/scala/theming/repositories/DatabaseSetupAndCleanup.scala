package theming.repositories

import org.scalatest.{BeforeAndAfterAll, Suite, SuiteMixin}
import slick.jdbc.MySQLProfile.api._
import theming.config.{DatabaseConfig, SchemaMigration}

import scala.concurrent.Await
import scala.concurrent.duration._

trait DatabaseSetupAndCleanup extends SuiteMixin with BeforeAndAfterAll {
  this: Suite =>

  def databaseConfig: DatabaseConfig

  override protected def beforeAll(): Unit = {
    new SchemaMigration(databaseConfig).run()
  }

  protected def deleteAll() {
    val deleteAllFuture = databaseConfig.database.run(DBIO.seq(
      sqlu"delete from user_roles",
      sqlu"delete from users",
      sqlu"delete from theme_configs",
      sqlu"delete from companies",
      sqlu"delete from themes"
    ))
    Await.result(deleteAllFuture, 5 seconds)
  }
}
