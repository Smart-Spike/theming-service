package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.{complete, get, pathPrefix}
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import theming.config.ApplicationConfig
import theming.routes.AuthRoutes
import theming.services.{TokenService, UserService}

import scala.concurrent.ExecutionContext

object ThemingApp extends App with ApplicationConfig {

  implicit val system: ActorSystem = ActorSystem("Theming_Service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  private val userService = new UserService()
  private val tokenService = new TokenService()
  private val authRoutes = new AuthRoutes(userService, tokenService).routes

  private val healthCheckRoute = pathPrefix("healthcheck") {
    get {
      complete("UP!")
    }
  }

  new HttpApp {
    override protected def routes: Route =
      pathPrefix("api") {
        healthCheckRoute ~
          authRoutes
      }
  }.startServer(httpHost, httpPort)

}
