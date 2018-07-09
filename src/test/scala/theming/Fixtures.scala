package theming

import theming.domain.{Company, Theme, User}

trait Fixtures {
  val testUser = User(Some("user-id"), "me@email.com", "password", None, Seq("USER"))
  val testTheme = Theme("DARK", Map("font" -> "large", "menu" -> "left"))
  val testCompany = Company(None, "Google", None)
}
