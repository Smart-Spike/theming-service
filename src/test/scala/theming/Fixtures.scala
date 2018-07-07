package theming

import theming.domain.{Theme, User}

trait Fixtures {
  val testUser = User(Some("user-id"), "me@email.com", "password", Seq("USER"))
  val testTheme = Theme(None, "Dark", Map("font" -> "large", "menu" -> "left"))
}
