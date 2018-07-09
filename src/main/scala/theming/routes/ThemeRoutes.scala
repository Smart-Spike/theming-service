package theming.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import theming.repositories.{CompanyRepository, ThemeRepository, UserRepository}
import theming.security.AuthenticationDirective.AuthenticationDirective

import scala.concurrent.ExecutionContext

class ThemeRoutes(authenticate: AuthenticationDirective,
                  themeRepository: ThemeRepository,
                  userRepository: UserRepository,
                  companyRepository: CompanyRepository)
                 (implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val DefaultThemeId = "LIGHT"

  val routes: Route = {
    pathPrefix("users" / Segment / "theme") { userId =>
      authenticate { auth =>
        authorize(auth.isAdmin || (auth.isUser && userId == auth.userId)) {
          get {
            onSuccess(userRepository.findById(userId)) {
              case None => complete(StatusCodes.NotFound)
              case Some(user) => user.companyId match {
                case None => complete(themeRepository.findById(DefaultThemeId))
                case Some(companyId) =>
                  onSuccess(companyRepository.findById(companyId)) { company =>
                    complete(themeRepository.findById(company.get.defaultThemeId.getOrElse(DefaultThemeId)))
                  }
              }
            }
          }
        }
      }
    }
  }

}
