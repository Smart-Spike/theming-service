package theming.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import theming.repositories.ThemeRepository
import theming.security.AuthenticationDirective.AuthenticationDirective

import scala.concurrent.ExecutionContext

class ThemeRoutes(authenticate: AuthenticationDirective, themeRepository: ThemeRepository)(implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val routes: Route = {
    pathPrefix("users" / Segment / "theme") { userId =>
      authenticate { auth =>
        authorize(auth.isAdmin || (auth.isUser && userId == auth.userId)) {
          get {
            complete(themeRepository.findById("DARK"))
          }
        }
      }
    }
  }

}
