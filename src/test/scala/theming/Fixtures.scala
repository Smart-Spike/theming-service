package theming

import theming.domain.User

trait Fixtures {
  val testUser = User(Some("user-id"), "me@email.com", "password", Seq("USER"))
}
