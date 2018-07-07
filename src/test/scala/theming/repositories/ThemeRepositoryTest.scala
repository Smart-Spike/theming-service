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

    it("returns None when there is no theme") {
      themeRepository.findById("non-existent-id").map { theme: Option[Theme] =>
        theme should not be defined
      }
    }

    it("returns theme when it exists") {
      for {
        savedTheme <- themeRepository.create(testTheme)
        foundTheme <- themeRepository.findById(savedTheme.id)
      } yield {
        foundTheme.get.id shouldBe testTheme.id
        foundTheme.get.config should contain theSameElementsAs testTheme.config
      }
    }

    it("returns theme with empty config") {
      for {
        savedTheme <- themeRepository.create(testTheme.copy(config = Map()))
        foundTheme <- themeRepository.findById(savedTheme.id)
      } yield {
        foundTheme shouldBe defined
        foundTheme.get.config shouldBe empty
      }
    }
  }
}
