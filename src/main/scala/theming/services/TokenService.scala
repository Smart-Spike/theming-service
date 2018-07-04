package theming.services

import io.circe._
import io.circe.syntax._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import theming.domain.User

class TokenService {

  private val tempKey = "mySuperSecretKey"

  implicit def userTokenEncoder: Encoder[User] =
    Encoder.forProduct2("userId", "roles")(u => (u.id, u.roles))

  def createToken(user: User): String = {
    val claim = JwtClaim(user.asJson.toString)
      .issuedNow
      .expiresIn(10 * 60)

    Jwt.encode(claim, tempKey, JwtAlgorithm.HS256)
  }

}
