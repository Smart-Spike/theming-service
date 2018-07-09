package theming

import akka.http.scaladsl.model.{ContentTypes, StatusCodes}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.scalatest._
import theming.config.ApplicationConfig
import theming.domain.Credentials
import theming.repositories.UserRepository

import scala.concurrent.ExecutionContextExecutor

class ThemingServiceTest extends AsyncFunSpec
  with Fixtures
  with ScalatestRouteTest
  with Matchers
  with ApplicationConfig {

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

  }


}
