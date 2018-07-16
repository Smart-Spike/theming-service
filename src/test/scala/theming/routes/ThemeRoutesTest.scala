package theming.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{AsyncFunSpec, Matchers}
import theming.Fixtures
import theming.domain.{Roles, Theme}
import theming.repositories.{CompanyRepository, ThemeRepository, UserRepository}
import theming.security.AuthenticationDirective
import theming.services.TokenService

import scala.concurrent.Future

class ThemeRoutesTest extends AsyncFunSpec
  with Fixtures
  with ScalatestRouteTest
  with Matchers {

  private val userRepository = Mockito.mock(classOf[UserRepository])
  private val companyRepository = Mockito.mock(classOf[CompanyRepository])
  private val themeRepository = Mockito.mock(classOf[ThemeRepository])

  private val tokenService = new TokenService

  private val routes = new ThemeRoutes(AuthenticationDirective(tokenService), themeRepository, userRepository, companyRepository).routes

  describe("theme") {
    val tokenService = new TokenService
    lazy val sealedRoutes = Route.seal(routes)

    when(userRepository.findByEmail(anyString())).thenReturn(Future.successful(Some(testUser)))
    when(userRepository.findById(anyString())).thenReturn(Future.successful(Some(testUser)))
    when(themeRepository.findById(anyString())).thenReturn(Future.successful(Some(testTheme)))

    it("returns unauthorized when there is no Authorization header") {
      Get("/users/user-id/theme") ~> sealedRoutes ~> check {
        status shouldBe StatusCodes.Unauthorized
      }
    }

    it("returns OK when token has correct user id and USER role") {
      val user = testUser.copy(roles = Seq(Roles.User))
      val token = tokenService.createToken(user)
      Get(s"/users/${user.id.get}/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
        status shouldBe StatusCodes.OK
        responseAs[Theme].id shouldBe "DARK"
      }
    }

    it("returns OK when user is PlatformAdmin and requests other user's theme") {
      val token = tokenService.createToken(testUser.copy(roles = Seq(Roles.PlatformAdmin)))
      Get(s"/users/some-ther-user/theme") ~> addHeader("Authorization", s"Bearer $token") ~> routes ~> check {
        status shouldBe StatusCodes.OK
      }
    }

    it("returns forbidden when userId in the path is different from one in token") {
      val token = tokenService.createToken(testUser)
      val someOtherUserId = "some-other-user-id"
      when(userRepository.findById(someOtherUserId))
        .thenReturn(Future.successful(Some(testUser.copy(id = Some(someOtherUserId)))))

      Get("/users/" + someOtherUserId + "/theme") ~> addHeader("Authorization", s"Bearer $token") ~> sealedRoutes ~> check {
        status shouldBe StatusCodes.Forbidden
      }
    }

    it("returns forbidden when company admin tries to access user of different company") {
      val currentUserCompany = testCompany.copy(id = Some("company1"))
      val currentUser = testUser.copy(company = Some(currentUserCompany), roles = Seq(Roles.CompanyAdmin))
      val token = tokenService.createToken(currentUser)

      val requestedUserId = "some-other-user-id"
      val requestedUserCompany = testCompany.copy(id = Some("company2"))
      val requestedUser = testUser.copy(company = Some(requestedUserCompany))
      when(userRepository.findById(requestedUserId)).thenReturn(Future.successful(Some(requestedUser)))

      Get("/users/" + requestedUserId + "/theme") ~> addHeader("Authorization", s"Bearer $token") ~> sealedRoutes ~> check {
        status shouldBe StatusCodes.Forbidden
      }
    }

    it("returns forbidden when has no roles") {
      val token = tokenService.createToken(testUser.copy(roles = Seq()))

      Get(s"/users/${testUser.id.get}/theme") ~> addHeader("Authorization", s"Bearer $token") ~> sealedRoutes ~> check {
        status shouldBe StatusCodes.Forbidden
      }
    }
  }
}
