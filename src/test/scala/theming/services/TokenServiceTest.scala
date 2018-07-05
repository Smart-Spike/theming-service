package theming.services

import io.circe.parser._
import org.scalatest.{FunSpec, Matchers}
import pdi.jwt.{Jwt, JwtOptions}
import theming.Fixtures

class TokenServiceTest extends FunSpec with Matchers with Fixtures {

  val tokenService = new TokenService

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

      it("should create token with roles") {
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

      it("should create token issued at and expiry") {
        val rawToken = tokenService.createToken(testUser)

        for {
          decoded <- Jwt.decode(rawToken, JwtOptions(signature = false))
          json <- parse(decoded)
          issuedAt <- json.hcursor.get[Long]("iat")
          expires <- json.hcursor.get[Long]("exp")
        } {
          issuedAt should not be 0
          (expires - issuedAt) should be(60 * 10)
        }
      }
    }

    describe("verify and extract Auth") {
      it("returns Auth for valid token") {
        val token = tokenService.createToken(testUser)

        val result = tokenService.verifyAndExtractAuth(token)

        result shouldBe defined
        result.get.userId shouldBe testUser.id.get
      }

      it("returns none for in valid token") {
        val result = tokenService.verifyAndExtractAuth("asdf")

        result should not be defined
      }
    }

  }

}
