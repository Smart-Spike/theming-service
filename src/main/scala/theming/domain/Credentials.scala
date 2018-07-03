package theming.domain

import io.circe.generic.semiauto.deriveDecoder
import io.circe.Decoder

case class Credentials(email: String, password: String)

object Credentials {
  implicit val credentialsDecoder: Decoder[Credentials] = deriveDecoder[Credentials]
}
