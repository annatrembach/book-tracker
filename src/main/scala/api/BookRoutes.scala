package api

import actor.StatsActor
import org.apache.pekko.actor.typed.scaladsl.AskPattern.*
import org.apache.pekko.util.Timeout

import scala.concurrent.duration.*
import repository.BookRepository
import service.BookService
import config.Database
import org.typelevel.doobie.*
import org.typelevel.doobie.implicits.*
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*
import cats.effect.unsafe.implicits.global
import org.apache.pekko.actor.typed.ActorSystem
import spray.json.enrichAny

class BookRoutes(
                  repository: BookRepository,
                  service: BookService,
                  statsActor: org.apache.pekko.actor.typed.ActorRef[StatsActor.Command],
                  system: ActorSystem[?]
                ):

  import JsonFormats.{*, given}

  given Timeout = 3.seconds
  given org.apache.pekko.actor.typed.Scheduler = system.scheduler

  val route =
    path("stats") {
      get {
        onSuccess(
          statsActor.ask[StatsActor.Stats] { replyTo =>
            StatsActor.GetStats(replyTo)
          }
        ) { stats =>
          complete(stats)
        }
      }
    } ~
    path("books") {
      get {
        onSuccess(
          repository.findAll
            .transact(Database.transactor)
            .unsafeToFuture()
        ) { books =>
          complete(books.toJson)
        }
      } ~
        post {
          entity(as[CreateBookRequest]) { book =>
            onSuccess(
              repository
                .save(book)
                .transact(Database.transactor)
                .unsafeToFuture()
            ) { savedBook =>
              complete(savedBook)
            }
          }
        }
    } ~
    path("books" / LongNumber) { id =>

      get {
        onSuccess(
          repository
            .findById(id)
            .transact(Database.transactor)
            .unsafeToFuture()
        ) {
          case Some(book) =>
            complete(book)

          case None =>
            complete(404, "Book not found")
        }
      } ~
      delete {
        onSuccess(
          repository
            .delete(id)
            .transact(Database.transactor)
            .unsafeToFuture()
        ) {
          case 1 =>
            complete(204, "Book deleted")

          case 0 =>
            complete(404, "Book not found")
        }
      } ~
      put {
        entity(as[CreateBookRequest]) { request =>
          onSuccess(
            repository
              .update(id, request)
              .transact(Database.transactor)
              .unsafeToFuture()
          ) {
            case Some(book) =>
              complete(book)

            case None =>
              complete(404, "Book not found")
          }
        }
      }
    } ~
    path("books" / LongNumber / "status") { id =>
      patch {
        entity(as[UpdateBookStatusRequest]) { request =>
          onSuccess(
            service
              .updateStatus(id, request.status)
              .transact(Database.transactor)
              .unsafeToFuture()
          ) {
            case Some(book) =>
              complete(book)

            case None =>
              complete(404, "Book not found")
          }
        }
      }
    }