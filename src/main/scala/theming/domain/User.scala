package theming.domain

import theming.domain.Roles.Role

case class User(id: Option[String], email: String, password: String, company: Option[Company], roles: Seq[Role])
