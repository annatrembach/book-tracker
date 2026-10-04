package config

import com.typesafe.config.ConfigFactory

object AppConfig:

  private val config = ConfigFactory.load()

  val dbUrl: String =
    config.getString("db.url")

  val dbUser: String =
    config.getString("db.user")

  val dbPassword: String =
    config.getString("db.password")