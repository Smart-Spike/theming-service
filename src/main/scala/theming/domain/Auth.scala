package theming.domain

case class Auth(userId: String, roles: Seq[String]) {
  def isAdmin: Boolean = roles.contains("ADMIN")
  def isUser: Boolean = roles.contains("USER")
}
