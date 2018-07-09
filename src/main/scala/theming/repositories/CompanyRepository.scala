package theming.repositories

import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import theming.domain.Company
import theming.repositories.IdGenerator.generateId

import scala.concurrent.{ExecutionContext, Future}

class CompanyRepository(db: Database)(implicit executionContext: ExecutionContext) {

  import CompanyRepository._

  def create(company: Company): Future[Company] = {
    val companyWithId = company.copy(id = Some(generateId))
    db.run(companies += companyWithId).map(_ => companyWithId)
  }

  def findById(id: String): Future[Option[Company]] =
    db.run(companies.filter(_.id === id).result.headOption)

  def findByName(name: String): Future[Option[Company]] =
    db.run(companies.filter(_.name === name).result.headOption)

}

object CompanyRepository {

  private[repositories] class Companies(tag: Tag) extends Table[Company](tag, "companies") {
    def id = column[Option[String]]("id", O.PrimaryKey)

    def name = column[String]("name", O.Unique)

    def defaultThemeId = column[String]("default_theme_id")

    def * = (id, name, defaultThemeId) <> ((Company.apply _).tupled, Company.unapply)
  }

  private[repositories] val companies = TableQuery[Companies]
}

