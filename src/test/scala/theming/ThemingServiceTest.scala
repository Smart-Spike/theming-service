package theming

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.testkit.TestDuration
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.scalatest._
import theming.config.ApplicationConfig
import theming.domain.{Credentials, Theme}
import theming.repositories.UserRepository
import theming.services.TokenService

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class ThemingServiceTest extends AsyncFunSpec
  with Fixtures
  with ScalatestRouteTest
  with Matchers
  with ApplicationConfig {

  implicit val timeout = RouteTestTimeout(10.seconds dilated)

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  lazy val routes: Route = new ThemingService(databaseConfig).routes

  val userRepository = new UserRepository(databaseConfig.database)

  describe("Theming service") {

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
          contentType shouldBe ContentTypes.`text/plain(UTF-8)`
          responseAs[String] should not be empty
        }
      }
    }

    describe("theme") {
      val tokenService = new TokenService
      lazy val sealedRoutes = Route.seal(routes)

      it("returns unauthorized when there is no Authorization header") {
        Get("/api/users/user-id/theme") ~> sealedRoutes ~> check {
          status shouldBe StatusCodes.Unauthorized
        }
      }
      it("returns OK when token has correct user id and USER role") {
        userRepository.findByEmail("user@feature-service.com") map { user =>
          val token = tokenService.createToken(user.get)
          Get(s"/api/users/${user.get.id.get}/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
            status shouldBe StatusCodes.OK
            responseAs[Theme].id shouldBe "ORANGE"
          }
        }
      }

      it("returns OK when user is an ADMIN and requests other user's theme") {
        val token = tokenService.createToken(testUser.copy(roles = Seq("ADMIN")))
        for {
          someOtherUser <- userRepository.findByEmail("user@some-company.com")
        } yield {
          Get(s"/api/users/${someOtherUser.get.id.get}/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
            status shouldBe StatusCodes.OK
          }
        }
      }

      it("returns forbidden when userId in the path is different from one in token") {
        val token = tokenService.createToken(testUser)

        Get("/api/users/some-other-user-id/theme") ~> addHeader("Authorization", s"Bearer $token") ~> sealedRoutes ~> check {
          status shouldBe StatusCodes.Forbidden
        }
      }

      it("returns forbidden when has no roles") {
        val token = tokenService.createToken(testUser.copy(roles = Seq()))

        Get(s"/api/users/${testUser.id.get}/theme") ~> addHeader("Authorization", s"Bearer $token") ~> sealedRoutes ~> check {
          status shouldBe StatusCodes.Forbidden
        }
      }
    }

  }


}
