package theming.services

import com.typesafe.scalalogging.Logger
import theming.domain.{Theme, User}
import theming.repositories.{ThemeRepository, UserRepository}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class TestDataInitializer(userRepository: UserRepository, themeRepository: ThemeRepository)(implicit executionContext: ExecutionContext) {

  val logger = Logger(getClass)

  def initialize(): Unit = {
    logger.info("initializing test users and themes...")

    val testUsers: Seq[User] = Seq(
      User(None, "admin@feature-service.com", "password123", Seq("USER", "ADMIN")),
      User(None, "user@feature-service.com", "password123", Seq("USER")),
      User(None, "user@some-company.com", "password123", Seq("USER"))
    )
    val userFutures = testUsers.map { user =>
      userRepository.findByEmail(user.email).map {
        case None => userRepository.create(user)
        case Some(_) => Future.successful()
      }
    }

    val themeFuture = themeRepository.findById("DARK").map {
      case Some(_) => Future.successful()
      case None => themeRepository.create(Theme("DARK", Map("font" -> "large", "menu" -> "left")))
    }
    Await.result(Future.sequence(themeFuture :: userFutures.toList), 5 seconds)
    logger.info("Done initializing test users and themes")
  }
}
