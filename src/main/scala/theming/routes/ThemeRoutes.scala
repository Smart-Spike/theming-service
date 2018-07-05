package theming.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.PathMatchers.Segment
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.Logger
import theming.domain.Auth

import scala.concurrent.ExecutionContext

class ThemeRoutes(authenticator: AsyncAuthenticator[Auth])(implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  val routes: Route = {
    pathPrefix("users" / Segment / "theme") { userId =>
      authenticateOAuth2Async("my-realm", authenticator) { auth =>
        authorize(auth.isAdmin || (auth.isUser && userId == auth.userId)) {
          get {
            logger.info(s"Auth context: $auth")
            complete(StatusCodes.OK)
          }
        }
      }
    }
  }

}
