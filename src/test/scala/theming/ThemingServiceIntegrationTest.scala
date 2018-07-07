package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.stream.ActorMaterializer
import org.scalatest._
import theming.config.ApplicationConfig

import scala.concurrent.{ExecutionContextExecutor, Future}

class ThemingServiceIntegrationTest extends AsyncFunSpec
  with Fixtures
  with Matchers
  with ApplicationConfig
  with BeforeAndAfterAll {

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContextExecutor = system.dispatcher

  var binding: Future[Http.ServerBinding] = _

  override protected def beforeAll(): Unit = {
    val themingService = new ThemingService(databaseConfig)
    binding = Http().bindAndHandle(themingService.routes, httpHost, httpPort)
  }

  override protected def afterAll(): Unit = {
    binding.map(_.unbind())
  }

  val BaseUrl = s"http://localhost:$httpPort/api/"

  describe("Theming service") {
    describe("healthcheck") {
      it("responds with UP!") {
        for {
          response <- Http().singleRequest(HttpRequest(uri = BaseUrl + "healthcheck"))
          body <- Unmarshal(response.entity).to[String]
        } yield {
          response.status shouldBe StatusCodes.OK
          body shouldBe "UP!"
        }
      }
    }
  }
}
