package theming.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import theming.domain.Theme
import theming.repositories.{CompanyRepository, ThemeRepository, UserRepository}
import theming.security.AuthenticationDirective.AuthenticationDirective

import scala.concurrent.ExecutionContext

class ThemeRoutes(authenticate: AuthenticationDirective,
                  themeRepository: ThemeRepository,
                  userRepository: UserRepository,
                  companyRepository: CompanyRepository)
                 (implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val routes: Route = {
    pathPrefix("users" / Segment / "theme") { userId =>
      authenticate { auth =>
        authorize(auth.isPlatformAdmin || auth.isCompanyAdmin || (auth.isUser && userId == auth.userId)) {
          get {
            onSuccess(userRepository.findById(userId)) {
              case None => complete(StatusCodes.NotFound)
              case Some(user) => user.company match {
                case None => complete(themeRepository.findById(Theme.Default))
                case Some(company) =>
                  complete(themeRepository.findById(company.defaultThemeId))
              }
            }
          }
        }
      }
    }
  }

}
