package theming.services

import org.scalatest.{FunSpec, Matchers}
import pdi.jwt.{Jwt, JwtAlgorithm}
import theming.domain.User

import scala.util.{Failure, Success}

class TokenServiceTest extends FunSpec with Matchers {

  val tokenService = new TokenService

  val testUser = User(Some("user-id"), "me@email.com", "password", Seq("USER"))

  describe("TokenService") {

    describe("create token") {
      it("should create token with userId") {
        val expectedUserId = "expected id"

        val rawToken = tokenService.createToken(testUser.copy(id = Some(expectedUserId)))

        Jwt.decode(rawToken, "mySuperSecretKey", Seq(JwtAlgorithm.HS256)) match {
          case Success(decodedToken: String) =>
            decodedToken should include (expectedUserId)
          case Failure(e) =>
            fail(e.getMessage)
        }
      }

      it("should create token with roles") {
        val expectedRoles = Seq("ADMIN", "USER")

        val rawToken = tokenService.createToken(testUser.copy(roles = expectedRoles))

        Jwt.decode(rawToken, "mySuperSecretKey", Seq(JwtAlgorithm.HS256)) match {
          case Success(decodedToken: String) =>
            expectedRoles.foreach { role =>
              decodedToken should include (role)
            }
          case Failure(e) =>
            fail(e.getMessage)
        }
      }
    }

  }

}
