package theming.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import theming.domain.{Company, User}
import theming.repositories.IdGenerator._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(db: Database)(implicit val executionContext: ExecutionContext) {

  def create(user: User): Future[User] = {
    val userId = generateId
    val userWithId = user.copy(id = Some(userId))
    db.run(DBIO.seq(
      users += UserRecord.from(userWithId),
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
    val query = (users.filter(predicate)
      joinLeft CompanyRepository.companies on (_.companyId === _.id)
      joinLeft userRoles on (_._1.id === _.userId))

    db.run(query.result).map {
      case Nil => None
      case userRecordsCompaniesAndRoles =>
        val ((userRecord, company), _) = userRecordsCompaniesAndRoles.head
        val roles = userRecordsCompaniesAndRoles collect {
          case (_, Some(UserRole(_, role))) => role
        }
        Some(userRecord.asUser(roles, company))
    }
  }

  private case class UserRecord(id: String, email: String, password: String, companyId: Option[String]) {
    def asUser(roles: Seq[String], company: Option[Company]): User = User(Some(id), email, password, company, roles)
  }

  private object UserRecord {
    def from(user: User) = UserRecord(user.id.get, user.email, user.password, user.company.flatMap(company => company.id))
  }

  private class Users(tag: Tag) extends Table[UserRecord](tag, "users") {
    def id = column[String]("id", O.PrimaryKey)

    def email = column[String]("email", O.Unique)

    def password = column[String]("password")

    def companyId = column[Option[String]]("company_id")

    def * = (id, email, password, companyId) <> ((UserRecord.apply _).tupled, UserRecord.unapply)

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
