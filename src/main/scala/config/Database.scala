package config

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.typelevel.doobie.hikari.HikariTransactor
import scala.concurrent.ExecutionContext

object Database:

  def createTransactor(
                        url: String,
                        user: String,
                        password: String
                      ): HikariTransactor[IO] =
    HikariTransactor
      .newHikariTransactor[IO](
        "org.postgresql.Driver",
        url,
        user,
        password,
        ExecutionContext.global,
        None
      )
      .allocated
      .unsafeRunSync()
      ._1

  val transactor: HikariTransactor[IO] =
    createTransactor(
      AppConfig.dbUrl,
      AppConfig.dbUser,
      AppConfig.dbPassword
    )