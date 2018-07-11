package theming.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import theming.domain.Roles.Role
import theming.domain.{Company, User}
import theming.repositories.CompanyRepository.companies
import theming.repositories.IdGenerator._

import scala.concurrent.{ExecutionContext, Future}

class UserRepository(db: Database)(implicit val executionContext: ExecutionContext) {

  def create(user: User): Future[User] = {
    val userId = generateId
    val userWithId = user.copy(id = Some(userId))
    db.run(DBIO.seq(
      users += createRecord(userWithId),
      userRoles ++= user.roles.map(role => UserRole(userId, role.name))
    ).transactionally).map(_ => userWithId)
  }

  def findByEmail(email: String): Future[Option[User]] = {
    findByPredicate(_.email === email)
  }

  def findById(id: String): Future[Option[User]] = {
    findByPredicate(_.id === id)
  }

  private def findByPredicate(predicate: Users => Rep[Option[Boolean]]): Future[Option[User]] = {
    val usersJoinCompaniesJoinRoles =
      (users.filter(predicate)
        joinLeft companies on(_.companyId === _.id)
        joinLeft userRoles on (_._1.id === _.userId))
      .map {
        case ((userRecord, companyRecord), userRoleRecord) => (userRecord, companyRecord, userRoleRecord.map(_.role))
      }

    db.run(usersJoinCompaniesJoinRoles.result).map {
      case Nil => None
      case userRecordsCompaniesAndRoles =>
        val (userRecord, company, _) = userRecordsCompaniesAndRoles.head
        val roles = userRecordsCompaniesAndRoles collect {
          case (_, _, Some(roleName)) => Role(roleName)
        }
        Some(createUser(userRecord, company, roles))
    }
  }

  private def createUser(userRecord: UserRecord, company: Option[Company], roles: Seq[Role]) =
    User(Some(userRecord.id), userRecord.email, userRecord.password, company, roles)

  private case class UserRecord(id: String, email: String, password: String, companyId: Option[String])

  private def createRecord(user: User) = UserRecord(user.id.get, user.email, user.password, user.company.flatMap(company => company.id))

  private class Users(tag: Tag) extends Table[UserRecord](tag, "users") {
    def id = column[String]("id", O.PrimaryKey)

    def email = column[String]("email", O.Unique)

    def password = column[String]("password")

    def companyId = column[Option[String]]("company_id")

    def * = (id, email, password, companyId).mapTo[UserRecord]

  }

  private val users = TableQuery[Users]

  private case class UserRole(userId: String, role: String)

  private class UserRoles(tag: Tag) extends Table[UserRole](tag, "user_roles") {
    def userId = column[String]("user_id")

    def role = column[String]("role")

    def * = (userId, role).mapTo[UserRole]
  }

  private val userRoles = TableQuery[UserRoles]
}
