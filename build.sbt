name := "SkurtTheftGuard"

version := "1.0"

scalaVersion := "2.11.8"

resolvers += Resolver.bintrayRepo("hseeberger", "maven")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.6" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.4.9",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.9",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.9",
  "com.typesafe" % "config" % "1.3.0",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
  "de.heikoseeberger" %% "akka-http-circe" % "1.10.0",
  "net.databinder" %% "dispatch-http" % "0.8.10",
  "org.json4s" %% "json4s-native" % "3.4.0",
  "io.orchestrate" % "orchestrate-client" % "0.12.1"
)