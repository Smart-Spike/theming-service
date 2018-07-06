package theming.repositories

import org.scalatest._
import slick.jdbc.MySQLProfile.api._
import theming.Fixtures
import theming.config.{ApplicationConfig, SchemaMigration}

import scala.concurrent.Await
import scala.concurrent.duration._

class UserRepositoryTest extends AsyncFunSpec
  with Matchers
  with ApplicationConfig
  with Fixtures
  with BeforeAndAfterAll
  with BeforeAndAfter {

  val database = databaseConfig.database
  val userRepository = new UserRepository(database)

  override protected def beforeAll(): Unit = {
    new SchemaMigration(databaseConfig).run()
  }

  before {
    val deleteAllFuture = database.run(DBIO.seq(
      sqlu"delete from user_roles",
      sqlu"delete from users"
    ))
    Await.result(deleteAllFuture, 5 seconds)
  }

  describe("UserRepository") {

    it("creates new user") {
      userRepository.create(testUser.copy(id = None)) map { user =>
        user.id shouldBe defined
      }
    }

    it("returns none when user by email does not exist") {
      userRepository.findByEmail("someone@mail.com") map { result =>
        result should not be defined
      }
    }

    it("finds user by email") {
      for {
        user <- userRepository.create(testUser.copy(id = None))
        result <- userRepository.findByEmail(testUser.email)
      } yield {
        result shouldBe defined
        result.get.id shouldBe user.id
        result.get.roles should contain only (user.roles: _*)
      }
    }

    it("finds user without roles by email") {
      for {
        user <- userRepository.create(testUser.copy(id = None, roles = Seq()))
        result <- userRepository.findByEmail(testUser.email)
      } yield {
        result shouldBe defined
        result.get.id shouldBe user.id
        result.get.roles shouldBe empty
      }
    }
  }
}
