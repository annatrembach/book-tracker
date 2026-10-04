package repository

import api.CreateBookRequest
import config.Database
import domain.ReadingStatus
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.testcontainers.containers.PostgreSQLContainer
import org.typelevel.doobie.implicits.*
import cats.effect.unsafe.implicits.global
import org.flywaydb.core.Flyway

class DoobieBookRepositorySpec
  extends AnyWordSpec
    with BeforeAndAfterAll:

  val postgres =
    new PostgreSQLContainer("postgres:17")

  override def beforeAll(): Unit =
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

      postgres.start()

      Flyway
        .configure()
        .dataSource(
          postgres.getJdbcUrl,
          postgres.getUsername,
          postgres.getPassword
        )
        .load()
        .migrate()

  override def afterAll(): Unit =
    postgres.stop()

  "DoobieBookRepository" should {

    "save and find a book" in {

      val repository =
        new DoobieBookRepository()

      val transactor =
        Database.createTransactor(
          postgres.getJdbcUrl,
          postgres.getUsername,
          postgres.getPassword
        )

      val book =
        repository
          .save(
            CreateBookRequest(
              title = "Clean Code",
              author = "Robert Martin",
              pages = 464
            )
          )
          .transact(transactor)
          .unsafeRunSync()

      assert(book.title == "Clean Code")
      assert(book.author == "Robert Martin")
      assert(book.pages == 464)
      assert(book.status == ReadingStatus.WantToRead)

      val found =
        repository
          .findById(book.id)
          .transact(transactor)
          .unsafeRunSync()

      assert(found.isDefined)
      assert(found.get.title == "Clean Code")
    }
  }