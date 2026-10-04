import actor.StatsActor
import api.BookRoutes
import config.{Database, FlywayMigration}
import domain.ReadingStatus
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import repository.DoobieBookRepository
import service.BookService
import cats.effect.unsafe.implicits.global
import org.typelevel.doobie.implicits.toConnectionIOOps

object BookTrackerApp:

    def main(args: Array[String]): Unit =

        FlywayMigration.migrate()

        implicit val system: ActorSystem[Nothing] =
            ActorSystem(Behaviors.empty, "BookTracker")

        val repository = new DoobieBookRepository()

        val initialStats =
            repository
              .findAll
              .transact(Database.transactor)
              .unsafeRunSync()
              .foldLeft(StatsActor.Stats(0, 0, 0)) { (stats, book) =>
                  book.status match
                      case ReadingStatus.WantToRead =>
                          stats.copy(wantToRead = stats.wantToRead + 1)

                      case ReadingStatus.Reading =>
                          stats.copy(reading = stats.reading + 1)

                      case ReadingStatus.Finished =>
                          stats.copy(finished = stats.finished + 1)
              }

        val statsActor =
            system.systemActorOf(StatsActor(), "stats")

        statsActor ! StatsActor.Initialize(initialStats)

        val service =
            new BookService(repository, statsActor)

        val routes =
            new BookRoutes(repository, service, statsActor, system)

        Http()
          .newServerAt("localhost", 8080)
          .bind(routes.route)