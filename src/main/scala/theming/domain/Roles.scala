package theming.domain

import io.circe.{Decoder, Encoder, HCursor, Json}

object Roles {

  sealed abstract class Role(val name: String)

  object PlatformAdmin extends Role("PLATFORM_ADMIN")

  object CompanyAdmin extends Role("COMPANY_ADMIN")

  object User extends Role("USER")

  object Role {
    def apply(name: String): Role = name match {
      case PlatformAdmin.name => PlatformAdmin
      case CompanyAdmin.name => CompanyAdmin
      case User.name => User
    }

    def unapply(role: Role): Option[String] = Some(role.name)

    implicit val encodeRole: Encoder[Role] = (role: Role) => Json.fromString(role.name)
    implicit val decodeRole: Decoder[Role] = (c: HCursor) => c.as[String].map(Role(_))

  }

}
