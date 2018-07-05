package theming.services

import com.typesafe.scalalogging.Logger
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import theming.domain.{Auth, User}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TokenService {

  val logger = Logger(getClass)

  private val tempKey = "mySuperSecretKey"

  implicit def userTokenEncoder: Encoder[User] =
    Encoder.forProduct2("userId", "roles")(u => (u.id, u.roles))

  def createToken(user: User): String = {
    val claim = JwtClaim(user.asJson.toString)
      .issuedNow
      .expiresIn((10 minutes).toSeconds)

    Jwt.encode(claim, tempKey, JwtAlgorithm.HS256)
  }

  def verifyAndExtractAuth(token: String): Option[Auth] = {
    // TODO: find a better way to chain/flatten Try -> Either -> Result
    val decoded = Jwt.decode(token, tempKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(decodedToken) => Some(decodedToken)
      case Failure(e) =>
        logger.info("unable to decode token", e.getMessage)
        None
    }
    decoded.map { decodedToken =>
      parse(decodedToken) match {
        case Right(json) => json
      }
    }.map { json =>
      json.as[Auth] match {
        case Right(auth) => auth
      }
    }
  }
}
