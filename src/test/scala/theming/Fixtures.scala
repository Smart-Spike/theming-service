package theming

import theming.domain.{Company, Roles, Theme, User}

trait Fixtures {
  val testUser = User(Some("user-id"), "me@email.com", "password", None, Seq(Roles.User))
  val testTheme = Theme("DARK", Map("font" -> "large", "menu" -> "left"))
  val testCompany = Company(None, "Google", testTheme.id)
}
