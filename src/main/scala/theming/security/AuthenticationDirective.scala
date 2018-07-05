package theming.security

import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{AsyncAuthenticator, authenticateOAuth2Async, provide}
import akka.http.scaladsl.server.directives.Credentials
import theming.domain.Auth
import theming.services.TokenService

import scala.concurrent.Future

object AuthenticationDirective {
  type AuthenticationDirective = Directive1[Auth]

  def apply(tokenService: TokenService): AuthenticationDirective = authenticate(tokenService)

  private def authenticate(tokenService: TokenService)(): AuthenticationDirective = {
    val authenticator: AsyncAuthenticator[Auth] = {
      case Credentials.Provided(token) => Future.successful(tokenService.verifyAndExtractAuth(token))
      case _ => Future.successful(None)
    }

    authenticateOAuth2Async("theming-service-realm", authenticator) flatMap { auth =>
      provide(auth)
    }
  }
}
