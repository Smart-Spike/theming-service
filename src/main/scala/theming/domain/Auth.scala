package theming.domain

case class Auth(userId: String, roles: Seq[Roles.Role], companyId: Option[String] = None) {
  val isPlatformAdmin: Boolean = roles.contains(Roles.PlatformAdmin)

  val isCompanyAdmin: Boolean = roles.contains(Roles.CompanyAdmin) && companyId.isDefined

  val isUser: Boolean = roles.contains(Roles.User)

  def hasAccessToUserResources(user: User): Boolean =
    isPlatformAdmin ||
      (isCompanyAdmin && user.company.isDefined && user.company.get.id == companyId) ||
      (isUser && user.id.get == userId)
}
