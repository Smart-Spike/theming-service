package theming.domain

case class Company(id: Option[String], name: String, defaultThemeId: String = Theme.Default)
