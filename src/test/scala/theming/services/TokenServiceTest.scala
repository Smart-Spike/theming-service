package theming.services

import io.circe.parser._
import org.scalatest.{FunSpec, Matchers}
import pdi.jwt.{Jwt, JwtOptions}
import theming.domain.User

class TokenServiceTest extends FunSpec with Matchers {

  val tokenService = new TokenService

  val testUser = User(Some("user-id"), "me@email.com", "password", Seq("USER"))

  describe("TokenService") {

    describe("create token") {
      it("should create token with userId") {
        val expectedUserId = "expected id"

        val rawToken = tokenService.createToken(testUser.copy(id = Some(expectedUserId)))

        for {
          decoded <- Jwt.decode(rawToken, JwtOptions(signature = false))
          json <- parse(decoded)
          userId <- json.hcursor.get[String]("userId")
        } {
          userId shouldBe expectedUserId
        }
      }

      it("should create token with rolesasd") {
        val expectedRoles = Seq("ADMIN", "USER")

        val rawToken = tokenService.createToken(testUser.copy(roles = expectedRoles))

        for {
          decoded <- Jwt.decode(rawToken, JwtOptions(signature = false))
          json <- parse(decoded)
          roles <- json.hcursor.get[Seq[String]]("roles")
        } {
          roles should contain only (expectedRoles: _*)
        }
      }
    }

  }

}
