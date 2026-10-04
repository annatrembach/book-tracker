scalaVersion := "3.9.0"

lazy val pekkoVersion = "1.7.0"
lazy val pekkoHttpVersion = "1.4.0"
lazy val doobieVersion = "1.0.0-RC13"
lazy val flywayVersion = "11.13.1"
lazy val testcontainersVersion = "1.21.4"

lazy val root = rootProject
  .settings(
    name := "book-tracker",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
      "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
      "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion,
      "org.apache.pekko" %% "pekko-http-spray-json" % pekkoHttpVersion,
      "org.typelevel" %% "doobie-core" % doobieVersion,
      "org.typelevel" %% "doobie-postgres" % doobieVersion,
      "org.typelevel" %% "doobie-hikari" % doobieVersion,
      "org.postgresql" % "postgresql" % "42.7.13",
      "org.flywaydb" % "flyway-core" % flywayVersion,
      "org.flywaydb" % "flyway-database-postgresql" % flywayVersion,
      "ch.qos.logback" % "logback-classic" % "1.5.18",
      "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
      "org.testcontainers" % "testcontainers" % testcontainersVersion % Test,
      "org.testcontainers" % "postgresql"     % testcontainersVersion % Test
    )
  )