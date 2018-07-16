package theming.services

import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import theming.domain.{Auth, User}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class TokenService extends LazyLogging {

  private val tempKey = "mySuperSecretKey"

  def createToken(user: User): String = {
    val auth = Auth(user.id.get, user.roles, user.company.flatMap(_.id))
    val claim = JwtClaim(auth.asJson.toString)
      .issuedNow
      .expiresIn((10 minutes).toSeconds)

    Jwt.encode(claim, tempKey, JwtAlgorithm.HS256)
  }

  def verifyAndExtractAuth(token: String): Option[Auth] = {
    Jwt.decode(token, tempKey, Seq(JwtAlgorithm.HS256)) match {
      case Success(claimAsString) =>
        decode[Auth](claimAsString) match {
          case Right(auth) => Option(auth)
          case Left(error) =>
            logger.info(s"error decoding parsing JWT claim JSON $error")
            None
        }
      case Failure(e) =>
        logger.info(s"unable to decode token ${e.getMessage}")
        None
    }
  }
}
