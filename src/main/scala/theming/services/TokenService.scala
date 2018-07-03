package theming.services

import io.circe.Encoder
import pdi.jwt.{Jwt, JwtAlgorithm}
import theming.domain.User

class TokenService {

  private val tempKey = "mySuperSecretKey"

  def encodeUserForTokenPayload: Encoder[User] =
    Encoder.forProduct2("userId", "roles")(u => (u.id, u.roles))

  def createToken(user: User): String = {
    Jwt.encode(encodeUserForTokenPayload(user).toString, tempKey, JwtAlgorithm.HS256)
  }

}
