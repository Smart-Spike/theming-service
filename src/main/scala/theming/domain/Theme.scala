package theming.domain

case class Theme(id: String, config: Map[String, String] = Map())

object Theme {
  val Default = "LIGHT"
}

