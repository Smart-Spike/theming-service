package theming.domain

case class User(id: Option[String], email: String, password: String, company: Option[Company], roles: Seq[String])
