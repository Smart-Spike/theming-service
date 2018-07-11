package theming.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import theming.domain.Theme

import scala.concurrent.{ExecutionContext, Future}

class ThemeRepository(db: Database)(implicit val executionContext: ExecutionContext) {

  def create(theme: Theme): Future[Theme] = {
    val inserts = DBIO.seq(
      themes += theme,
      themeConfigs ++= theme.config.map({
        case (key, value) => ThemeConfig(theme.id, key, value)
      })
    )
    db.run(inserts.transactionally).map(_ => theme)
  }

  def findById(id: String): Future[Option[Theme]] = {
    val query = themes.filter(_.id === id) joinLeft
      themeConfigs on (_.id === _.themeId)
    db.run(query.result).map {
      case Nil => None
      case themeAndConfigs =>
        val (theme, _) = themeAndConfigs.head
        val config = themeAndConfigs.collect {
          case (_, Some(ThemeConfig(_, key, value))) => key -> value
        }.toMap
        Some(theme.copy(config = config))
    }
  }

  private class Themes(tag: Tag) extends Table[Theme](tag, "themes") {
    def id = column[String]("id", O.PrimaryKey)

    def * = id <> (id => Theme(id), (theme: Theme) => Some(theme.id))
  }

  private val themes = TableQuery[Themes]

  private case class ThemeConfig(themeId: String, key: String, value: String)

  private class ThemeConfigs(tag: Tag) extends Table[ThemeConfig](tag, "theme_configs") {
    def themeId = column[String]("theme_id")

    def key = column[String]("config_key")

    def value = column[String]("config_value")

    def * = (themeId, key, value).mapTo[ThemeConfig]
  }

  private val themeConfigs = TableQuery[ThemeConfigs]

}
