package service

import actor.StatsActor
import api.CreateBookRequest
import domain.{Book, ReadingStatus}
import org.apache.pekko.actor.testkit.typed.scaladsl.ActorTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.wordspec.AnyWordSpec
import org.typelevel.doobie.ConnectionIO
import org.typelevel.doobie.implicits.*
import org.typelevel.doobie.free.connection.pure
import cats.effect.unsafe.implicits.global
import repository.BookRepository

class BookServiceSpec
  extends AnyWordSpec
    with BeforeAndAfterAll:

  val testKit = ActorTestKit()

  override def beforeAll(): Unit =
    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"))

  override def afterAll(): Unit =
    testKit.shutdownTestKit()

  "BookService" should {

    "send StatusChanged when book status changes" in {

      val repository = new BookRepository:

        override def findAll: ConnectionIO[List[Book]] =
          pure(Nil)

        override def findById(id: Long): ConnectionIO[Option[Book]] =
          pure(
            Some(
              Book(
                id = 1,
                title = "Clean Code",
                author = "Robert Martin",
                pages = 464,
                status = ReadingStatus.Reading
              )
            )
          )

        override def save(
                           book: CreateBookRequest
                         ): ConnectionIO[Book] =
          pure(
            Book(
              id = 1,
              title = book.title,
              author = book.author,
              pages = book.pages,
              status = ReadingStatus.WantToRead
            )
          )

        override def delete(id: Long): ConnectionIO[Int] =
          pure(1)

        override def update(
                             id: Long,
                             book: CreateBookRequest
                           ): ConnectionIO[Option[Book]] =
          pure(None)

        override def updateStatus(
                                   id: Long,
                                   status: ReadingStatus
                                 ): ConnectionIO[Option[Book]] =
          pure(
            Some(
              Book(
                id = 1,
                title = "Clean Code",
                author = "Robert Martin",
                pages = 464,
                status = status
              )
            )
          )

      val actor =
        testKit.spawn(StatsActor())

      val probe =
        testKit.createTestProbe[StatsActor.Command]()

      val service =
        new BookService(repository, probe.ref)

      service
        .updateStatus(1, ReadingStatus.Finished)
        .transact(config.Database.transactor)
        .unsafeRunSync()

      val message =
        probe.receiveMessage()

      message match
        case StatsActor.StatusChanged(oldStatus, newStatus) =>
          assert(oldStatus == ReadingStatus.Reading)
          assert(newStatus == ReadingStatus.Finished)

        case _ =>
          fail("Expected StatusChanged message")
    }
  }