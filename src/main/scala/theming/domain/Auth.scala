package theming.domain

case class Auth(userId: String, roles: Seq[Roles.Role]) {
  val isPlatformAdmin: Boolean = roles.contains(Roles.PlatformAdmin)

  val isCompanyAdmin: Boolean = roles.contains(Roles.CompanyAdmin)

  val isUser: Boolean = roles.contains(Roles.User)
}
