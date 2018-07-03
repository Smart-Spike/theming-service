package theming.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import theming.domain.Credentials
import theming.services.{TokenService, UserService}

class AuthRoutes(userService: UserService, tokenService: TokenService) extends FailFastCirceSupport {

  val routes: Route = {
    pathPrefix("login") {
      post {
        decodeRequest {
          entity(as[Credentials]) { credentials =>
            onSuccess(userService.findByCredentials(credentials)) {
              case Some(user) => complete(tokenService.createToken(user))
              case None => complete(StatusCodes.Unauthorized)
            }
          }
        }
      }
    }
  }

}
