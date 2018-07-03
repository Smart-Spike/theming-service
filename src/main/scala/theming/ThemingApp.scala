package theming

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{HttpApp, Route}
import akka.stream.ActorMaterializer
import theming.config.ApplicationConfig

import scala.concurrent.ExecutionContext

object ThemingApp extends App with ApplicationConfig {

  implicit val system: ActorSystem = ActorSystem("Theming_Service")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executor: ExecutionContext = system.dispatcher

  new HttpApp {
    override protected def routes: Route = pathPrefix("api") {
      pathPrefix("hello") {
        pathEndOrSingleSlash {
          get {
            complete("Hello world!")
          }
        }
      }
    }
  }.startServer(httpHost, httpPort)

}
