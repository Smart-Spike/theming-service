name := "theming-service"

version := "0.1"

scalaVersion := "2.12.6"

val akkaHttpVersion = "10.1.3"
val circeVersion = "0.9.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.13",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
  "com.pauldijou" %% "jwt-core" % "0.16.0",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "org.liquibase" % "liquibase-core" % "3.0.5",
  "com.h2database" % "h2" % "1.4.197" % Test,
  "mysql" % "mysql-connector-java" % "8.0.11",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0"
)

enablePlugins(JavaAppPackaging)

parallelExecution in Test := false

version in Docker := "latest"
dockerRepository := Some("smartspike")
dockerExposedPorts := Seq(9000)