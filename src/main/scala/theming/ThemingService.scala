package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import theming.config.ApplicationConfig
import theming.routes.{AuthRoutes, ThemeRoutes}
import theming.security.AuthenticationDirective
import theming.services.{TokenService, UserService}

import scala.concurrent.ExecutionContext

trait ThemingService {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val executor: ExecutionContext

  private val userService = new UserService()
  private val tokenService = new TokenService()
  private val authRoutes = new AuthRoutes(userService, tokenService).routes
  private val themeRoutes = new ThemeRoutes(AuthenticationDirective(tokenService)).routes

  private val healthCheckRoute = pathPrefix("healthcheck") {
    get {
      complete("UP!")
    }
  }

  val routes: Route =
    pathPrefix("api") {
      healthCheckRoute ~
        authRoutes ~
        themeRoutes
    }
}

object Main extends App with ThemingService with ApplicationConfig {
  override implicit val system: ActorSystem = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val executor: ExecutionContext = system.dispatcher

  Http().bindAndHandle(routes, httpHost, httpPort)
}
