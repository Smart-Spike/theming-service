package theming.repositories

import org.scalatest.{AsyncFunSpec, BeforeAndAfter, Matchers}
import theming.Fixtures
import theming.config.ApplicationConfig

class CompanyRepositoryTest extends AsyncFunSpec
  with Matchers
  with ApplicationConfig
  with Fixtures
  with BeforeAndAfter
  with DatabaseSetupAndCleanup {

  val companyRepository = new CompanyRepository(databaseConfig.database)
  val themeRepository = new ThemeRepository(databaseConfig.database)

  before {
    deleteAll()
    themeRepository.create(testTheme)
  }

  describe("CompanyRepository") {

    it("creates company") {
      companyRepository.create(testCompany).map { company =>
        company.id shouldBe defined
      }
    }

    it("finds company") {
      for {
        saved <- companyRepository.create(testCompany)
        found <- companyRepository.findById(saved.id.get)
      } yield {
        found shouldBe defined
        found.get.id shouldBe saved.id
        found.get.name shouldBe saved.name
      }
    }

    it("returns none when company doesn't exist") {
      companyRepository.findById("doesnt-exist").map { result =>
        result should not be defined
      }
    }

  }


}
