scalaVersion := "2.12.6"
name := "rescue-data-importer"
organization := "ch.epfl.scala"
version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)

mainClass in (Compile, run) := Some("Seeder")
mainClass in (Compile, packageBin) := Some("Seeder")

libraryDependencies += "com.typesafe.play" %% "play-json-joda" % "2.6.9"
libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.6.17"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.9"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.24"
