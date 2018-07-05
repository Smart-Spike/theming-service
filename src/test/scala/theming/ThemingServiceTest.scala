package theming

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest._
import theming.domain.Credentials
import theming.services.TokenService

import scala.concurrent.ExecutionContextExecutor

class ThemingServiceTest extends AsyncFunSpec
  with ThemingService
  with Fixtures
  with ScalatestRouteTest
  with Matchers
  with FailFastCirceSupport {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  describe("Theming service") {

    describe("healthcheck") {
      it("responds with UP!") {
        Get("/api/healthcheck") ~> routes ~> check {
          responseAs[String] shouldEqual "UP!"
          status shouldBe StatusCodes.OK
        }
      }
    }

    describe("login") {
      it("returns Unauthorized for unknown user") {
        Post("/api/login", Credentials("hacker@hack.com", "123")) ~> routes ~> check {
          status shouldBe StatusCodes.Unauthorized
        }

      }

      it("returns Unauthorized for wrong password") {
        Post("/api/login", Credentials("admin@feature-service.com", "Wrong!")) ~> routes ~> check {
          status shouldBe StatusCodes.Unauthorized
        }
      }

      it("returns Token for correct credentials") {
        Post("/api/login", Credentials("admin@feature-service.com", "password123")) ~> routes ~> check {
          status shouldBe StatusCodes.OK
          responseAs[String] should not be empty
        }
      }
    }

    describe("theme") {
      val tokenService = new TokenService

      it("returns unauthorized when there is no Authorization header") {
        Get("/api/users/user-id/theme") ~> Route.seal(routes) ~> check {
          status shouldBe StatusCodes.Unauthorized
        }
      }
      it("returns OK when Authorization header is valid") {
        val token = tokenService.createToken(testUser)

        Get("/api/users/user-id/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
          status shouldBe StatusCodes.OK
        }
      }
    }

  }


}
