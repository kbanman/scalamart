name := "scalamart"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "codes.reactive" %% "scala-time" % "0.4.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.google.inject" % "guice" % "4.1.0",
  "io.getquill" %% "quill-async" % "2.0.0",
  "org.mockito" % "mockito-core" % "2.11.0" % Test,
  "org.scalactic" %% "scalactic" % "3.0.4",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)