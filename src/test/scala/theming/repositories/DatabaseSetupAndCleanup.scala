package theming.repositories

import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Suite, SuiteMixin}
import slick.jdbc.MySQLProfile.api._
import theming.config.{DatabaseConfig, SchemaMigration}

import scala.concurrent.Await
import scala.concurrent.duration._

trait DatabaseSetupAndCleanup extends SuiteMixin with BeforeAndAfter with BeforeAndAfterAll {
  this: Suite =>

  def databaseConfig: DatabaseConfig

  override protected def beforeAll(): Unit = {
    new SchemaMigration(databaseConfig).run()
  }

  before {
    val deleteAllFuture = databaseConfig.database.run(DBIO.seq(
      sqlu"delete from user_roles",
      sqlu"delete from users",
      sqlu"delete from theme_configs",
      sqlu"delete from themes"
    ))
    Await.result(deleteAllFuture, 5 seconds)
  }
}
