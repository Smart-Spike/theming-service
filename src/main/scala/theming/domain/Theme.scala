package theming.domain

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Theme(theme: String, config: Map[String, String])

object Theme {
  implicit val themeDecoder: Decoder[Theme] = deriveDecoder[Theme]
  implicit val themeEncoder: Encoder[Theme] = deriveEncoder[Theme]
}

