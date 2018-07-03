package theming.services

import java.util.UUID

import theming.domain.{Credentials, User}

import scala.concurrent.Future

class UserService {

  val users: Seq[User] = Seq(
    User(Some(UUID.randomUUID().toString), "admin@feature-service.com", "password123", Seq("USER", "ADMIN")),
    User(Some(UUID.randomUUID().toString), "user@feature-service.com", "password123", Seq("USER")),
    User(Some(UUID.randomUUID().toString), "user@some-company.com", "password123", Seq("USER"))
  )

  def findByCredentials(credentials: Credentials): Future[Option[User]] =
    users.find(_.email == credentials.email) match {
      case Some(user) if user.password == credentials.password => Future.successful(Some(user))
      case _ => Future.successful(None)
    }

}
