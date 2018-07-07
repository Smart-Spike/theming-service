package theming.domain

case class Theme(id: Option[String], name: String, config: Map[String, String] = Map())

