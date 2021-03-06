package theming.services

import com.typesafe.scalalogging.LazyLogging
import theming.domain.{Company, Roles, Theme, User}
import theming.repositories.{CompanyRepository, ThemeRepository, UserRepository}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class TestDataInitializer(userRepository: UserRepository,
                          themeRepository: ThemeRepository,
                          companyRepository: CompanyRepository)
                         (implicit executionContext: ExecutionContext) extends LazyLogging {

  val TestThemes = Map(
    "DARK" -> Map("font" -> "large", "menu" -> "left"),
    "LIGHT" -> Map("font" -> "small", "layout" -> "tiles"),
    "ORANGE" -> Map("font" -> "small", "layout" -> "list")
  )

  val TestCompanies: Seq[Company] = Seq(
    Company(None, "Google", "DARK"),
    Company(None, "Amazon", "ORANGE"),
    Company(None, "Microsoft", "LIGHT")
  )

  def initialize(): Unit = {
    logger.info("initializing test users and themes...")

    val setupFuture: Future[Unit] = for {
      themes <- createThemes()
      companies <- createCompanies()
      users <- createUsers(companies)
    } yield ()

    Await.result(setupFuture, 5 seconds)
    logger.info("Done initializing test users and themes")
  }

  private def createUsers(companies: Iterable[Company]): Future[Iterable[User]] = {
    val testUsers: Seq[User] = Seq(
      User(None, "admin@feature-service.com", "password123", None, Seq(Roles.PlatformAdmin)),
      User(None, "admin@google.com", "password123", Some(companies.find(_.name == "Google").head), Seq(Roles.User, Roles.CompanyAdmin)),
      User(None, "user@google.com", "password123", Some(companies.find(_.name == "Google").head), Seq(Roles.User)),
      User(None, "user@amazon.com", "password123", Some(companies.find(_.name == "Amazon").head), Seq(Roles.User)),
      User(None, "user@microsoft.com", "password123", Some(companies.filter(_.name == "Microsoft").head), Seq(Roles.PlatformAdmin))
    )

    def saveOrRetrieve(user: User): Future[User] =
      for {
        existingUser <- userRepository.findByEmail(user.email)
        result <-
          if (existingUser.isEmpty) userRepository.create(user)
          else Future.successful(existingUser.get)
      } yield {
        logger.info(result.toString)
        result
      }

    Future.sequence(testUsers.map(saveOrRetrieve))
  }

  private def createCompanies(): Future[Iterable[Company]] = {
    def saveOrRetrieve(company: Company) =
      for {
        existingCompany <- companyRepository.findByName(company.name)
        result: Company <-
          if (existingCompany.isEmpty) companyRepository.create(company)
          else Future.successful(existingCompany.get)
      } yield {
        logger.info(result.toString)
        result
      }

    Future.sequence(TestCompanies.map(saveOrRetrieve))
  }

  private def createThemes(): Future[Iterable[Theme]] = {
    def saveOrRetreive(theme: (String, Map[String, String])): Future[Theme] = {
      val (themeId, config) = theme
      for {
        existingTheme <- themeRepository.findById(themeId)
        result: Theme <-
          if (existingTheme.isEmpty) themeRepository.create(Theme(themeId, config))
          else Future.successful(existingTheme.get)
      } yield {
        logger.info(result.toString)
        result
      }
    }

    Future.sequence(TestThemes.map(saveOrRetreive))
  }
}
