package theming.services

import theming.domain.{Credentials, User}
import theming.repositories.UserRepository

import scala.concurrent.{ExecutionContext, Future}

class UserService(userRepository: UserRepository)(implicit executionContext: ExecutionContext) {

  def findByCredentials(credentials: Credentials): Future[Option[User]] = {
    userRepository.findByEmail(credentials.email).map {
      case Some(user) if user.password == credentials.password => Some(user)
      case _ => None
    }
  }

}
