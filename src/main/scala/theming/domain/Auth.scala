package theming.domain

case class Auth(userId: String, roles: Seq[Roles.Role]) {
  def isPlatformAdmin: Boolean = roles.contains(Roles.PlatformAdmin)

  def isCompanyAdmin: Boolean = roles.contains(Roles.CompanyAdmin)

  def isUser: Boolean = roles.contains(Roles.User)
}
