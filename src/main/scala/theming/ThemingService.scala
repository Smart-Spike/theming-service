package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.LazyLogging
import theming.config.{ApplicationConfig, DatabaseConfig, SchemaMigration}
import theming.repositories.{CompanyRepository, ThemeRepository, UserRepository}
import theming.routes.{AuthRoutes, ThemeRoutes}
import theming.security.AuthenticationDirective
import theming.services.{TestDataInitializer, TokenService, UserService}

import scala.concurrent.ExecutionContext

class ThemingService(databaseConfig: DatabaseConfig)
                    (implicit system: ActorSystem, materializer: ActorMaterializer, executor: ExecutionContext) {
  new SchemaMigration(databaseConfig).run()

  private val userRepository = new UserRepository(databaseConfig.database)
  private val themeRepository = new ThemeRepository(databaseConfig.database)
  private val companyRepository = new CompanyRepository(databaseConfig.database)

  new TestDataInitializer(userRepository, themeRepository, companyRepository).initialize()

  private val userService = new UserService(userRepository)
  private val tokenService = new TokenService()
  private val authRoutes = new AuthRoutes(userService, tokenService).routes

  private val themeRoutes = new ThemeRoutes(AuthenticationDirective(tokenService), themeRepository, userRepository, companyRepository).routes

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

object Main extends App with ApplicationConfig with LazyLogging {
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  private val themingService = new ThemingService(databaseConfig)
  Http().bindAndHandle(themingService.routes, httpHost, httpPort)

  logger.info(s"Server started on port $httpPort")
}
