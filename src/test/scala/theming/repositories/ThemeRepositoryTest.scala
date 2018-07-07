package theming.repositories

import org.scalatest.{AsyncFunSpec, Matchers}
import theming.Fixtures
import theming.config.ApplicationConfig
import theming.domain.Theme

class ThemeRepositoryTest extends AsyncFunSpec
  with Matchers
  with ApplicationConfig
  with Fixtures
  with DatabaseSetupAndCleanup {

  val themeRepository = new ThemeRepository(databaseConfig.database)

  describe("ThemeRepository") {
    it("saves theme without config") {
      themeRepository.create(testTheme).map { theme =>
        theme.id shouldBe defined
      }
    }

    describe("find by id") {
      it("returns None when there is no theme") {
        themeRepository.findById("non-existent-id").map { theme: Option[Theme] =>
          theme should not be defined
        }
      }

      it("returns theme when it exists") {
        for {
          savedTheme <- themeRepository.create(testTheme)
          foundTheme <- themeRepository.findById(savedTheme.id.get)
        } yield {
          foundTheme shouldBe defined
          foundTheme.get.name shouldBe testTheme.name
          foundTheme.get.config should contain theSameElementsAs testTheme.config
        }
      }

      it("returns theme with empty config") {
        for {
          savedTheme <- themeRepository.create(testTheme.copy(config = Map()))
          foundTheme <- themeRepository.findById(savedTheme.id.get)
        } yield {
          foundTheme shouldBe defined
          foundTheme.get.config shouldBe empty
        }
      }
    }
  }
}
