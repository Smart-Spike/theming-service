package theming.routes

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import theming.domain.Theme
import theming.security.AuthenticationDirective.AuthenticationDirective

import scala.concurrent.ExecutionContext

class ThemeRoutes(authenticate: AuthenticationDirective)(implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val routes: Route = {
    pathPrefix("users" / Segment / "theme") { userId =>
      authenticate { auth =>
        authorize(auth.isAdmin || (auth.isUser && userId == auth.userId)) {
          get {
            logger.info(s"Auth context: $auth")
            complete(Theme(None, "DARK", Map("font" -> "large")))
          }
        }
      }
    }
  }

}
