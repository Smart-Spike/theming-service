package theming.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import theming.domain.User
import theming.repositories.IdGenerator._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(db: Database)(implicit val executionContext: ExecutionContext) {

  def create(user: User): Future[User] = {
    val userId = generateId
    val userWithId = user.copy(id = Some(userId))
    db.run(DBIO.seq(
      users += userWithId,
      userRoles ++= user.roles.map(UserRole(userId, _))
    ).transactionally).map(_ => userWithId)
  }

  def findByEmail(email: String): Future[Option[User]] = {
    findByPredicate(_.email === email)
  }

  def findById(id: String): Future[Option[User]] = {
    findByPredicate(_.id === id)
  }

  private def findByPredicate(predicate: Users => Rep[Option[Boolean]]): Future[Option[User]] = {
    val usersWithRoles =
      users.filter(predicate) joinLeft userRoles on (_.id === _.userId)

    db.run(usersWithRoles.result).map {
      case Nil => None
      case usersAndRoles =>
        val (user, _) = usersAndRoles.head
        val roles = usersAndRoles collect {
          case (_, Some(UserRole(_, role))) => role
        }
        Some(user.copy(roles = roles))
    }
  }

  private class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Option[String]]("id", O.PrimaryKey)

    def email = column[String]("email", O.Unique)

    def password = column[String]("password")

    def companyId = column[Option[String]]("company_id")

    def * = (id, email, password, companyId) <> ((constructUser _).tupled, extractUser)

    private def constructUser(id: Option[String], email: String, password: String, companyId: Option[String]) = User(id, email, password, companyId, Seq())

    private def extractUser(user: User) = Some((user.id, user.email, user.password, user.companyId))
  }

  private val users = TableQuery[Users]

  private case class UserRole(userId: String, role: String)

  private class UserRoles(tag: Tag) extends Table[UserRole](tag, "user_roles") {
    def userId = column[String]("user_id")

    def role = column[String]("role")

    def * = (userId, role) <> ((UserRole.apply _).tupled, UserRole.unapply)
  }

  private val userRoles = TableQuery[UserRoles]
}
