package theming.domain

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class Auth(userId: String, roles: Seq[String]) {
  def isAdmin: Boolean = roles.contains("ADMIN")
  def isUser: Boolean = roles.contains("USER")
}

object Auth {
  implicit val authDecoder: Decoder[Auth] = deriveDecoder[Auth]
  implicit val authEncoder: Encoder[Auth] = deriveEncoder[Auth]
}
