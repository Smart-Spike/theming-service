package theming.domain

case class User(id: Option[String], email: String, password: String, roles: Seq[String])
