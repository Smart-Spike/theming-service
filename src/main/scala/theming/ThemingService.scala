package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import theming.config.{ApplicationConfig, DatabaseConfig, SchemaMigration}
import theming.domain.User
import theming.repositories.UserRepository
import theming.routes.{AuthRoutes, ThemeRoutes}
import theming.security.AuthenticationDirective
import theming.services.{TokenService, UserService}

import scala.concurrent.ExecutionContext

class ThemingService(databaseConfig: DatabaseConfig)
                    (implicit system: ActorSystem, materializer: ActorMaterializer, executor: ExecutionContext) {
  private val userRepository = new UserRepository(databaseConfig.database)

  private def initializeTestUsers(): Unit = {
    val testUsers: Seq[User] = Seq(
      User(None, "admin@feature-service.com", "password123", Seq("USER", "ADMIN")),
      User(None, "user@feature-service.com", "password123", Seq("USER")),
      User(None, "user@some-company.com", "password123", Seq("USER"))
    )
    testUsers.foreach(userRepository.create)
  }

  initializeTestUsers()

  private val userService = new UserService(userRepository)
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

object Main extends App with ApplicationConfig {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  new SchemaMigration(databaseConfig).run()

  private val themingService = new ThemingService(databaseConfig)
  Http().bindAndHandle(themingService.routes, httpHost, httpPort)
}
