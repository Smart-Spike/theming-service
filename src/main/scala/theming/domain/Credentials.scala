package theming.domain

import io.circe._
import io.circe.generic.semiauto._

case class Credentials(email: String, password: String)

object Credentials {
  implicit val credentialsDecoder: Decoder[Credentials] = deriveDecoder[Credentials]
  implicit val credentialsEncoder: Encoder[Credentials] = deriveEncoder[Credentials]
}
