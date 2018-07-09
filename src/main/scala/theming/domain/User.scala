package theming.domain

case class User(id: Option[String], email: String, password: String, companyId: Option[String], roles: Seq[String])
