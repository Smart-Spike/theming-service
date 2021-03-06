package theming.repositories

import org.scalatest._
import theming.Fixtures
import theming.config.ApplicationConfig

class UserRepositoryTest extends AsyncFunSpec
  with Matchers
  with ApplicationConfig
  with Fixtures
  with DatabaseSetupAndCleanup
  with BeforeAndAfter {

  val userRepository = new UserRepository(databaseConfig.database)

  val companyRepository = new CompanyRepository(databaseConfig.database)

  val themeRepository = new ThemeRepository(databaseConfig.database)

  before {
    deleteAll()
    themeRepository.create(testTheme)
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

    it("saves and retrieves user with company") {
      for {
        company <- companyRepository.create(testCompany)
        user <- userRepository.create(testUser.copy(company = Some(company)))
        foundUser <- userRepository.findByEmail(user.email)
      } yield {
        foundUser.get.company shouldBe defined
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

    it("finds user by id") {
      for {
        user <- userRepository.create(testUser.copy(id = None))
        result <- userRepository.findById(user.id.get)
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
