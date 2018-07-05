package theming.security

import akka.http.scaladsl.server.directives.Credentials
import theming.domain.Auth
import theming.services.TokenService

import scala.concurrent.Future

object Authenticator {

  def tokenAuthenticator(tokenService: TokenService)(credentials: Credentials): Future[Option[Auth]] = {
    credentials match {
      case Credentials.Provided(token) => Future.successful(tokenService.verifyAndExtractAuth(token))
      case _ => Future.successful(None)
    }
  }

}
