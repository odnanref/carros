name := """carro"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.22"

libraryDependencies ++= Seq(
  //jdbc,
  cache,
  ws,
  specs2 % Test
)

libraryDependencies ++= Seq(
"com.typesafe.play" %% "play-slick" % "1.1.1",
"com.typesafe.play" %% "play-slick-evolutions" % "1.1.1"
)

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.21"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.21"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.0"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.0"


