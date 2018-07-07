package theming

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.scalatest._
import theming.config.{ApplicationConfig, SchemaMigration}
import theming.domain.{Credentials, Theme}
import theming.repositories.UserRepository
import theming.services.TokenService

import scala.concurrent.ExecutionContextExecutor

class ThemingServiceTest extends AsyncFunSpec
  with Fixtures
  with ScalatestRouteTest
  with Matchers
  with ApplicationConfig
  with BeforeAndAfterAll {

  override implicit val executor: ExecutionContextExecutor = system.dispatcher

  lazy val routes: Route = new ThemingService(databaseConfig).routes

  val userRepository = new UserRepository(databaseConfig.database)

  override protected def beforeAll(): Unit = {
    new SchemaMigration(databaseConfig).run()
  }

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
            responseAs[Theme].id shouldBe "DARK"
          }
        }
      }

      it("returns OK when user is an ADMIN and requests other user's theme") {
        val token = tokenService.createToken(testUser.copy(roles = Seq("ADMIN")))

        Get("/api/users/some-other-user/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
          status shouldBe StatusCodes.OK
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
