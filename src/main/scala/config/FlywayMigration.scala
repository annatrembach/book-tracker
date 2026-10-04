package config

import org.flywaydb.core.Flyway

object FlywayMigration:

  def migrate(): Unit =
    Flyway
      .configure()
      .dataSource(
        AppConfig.dbUrl,
        AppConfig.dbUser,
        AppConfig.dbPassword
      )
      .load()
      .migrate()