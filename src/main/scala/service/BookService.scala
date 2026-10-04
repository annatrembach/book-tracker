package service

import actor.StatsActor
import domain.{Book, ReadingStatus}
import repository.BookRepository
import org.typelevel.doobie.*

class BookService(
                   repository: BookRepository,
                   statsActor: org.apache.pekko.actor.typed.ActorRef[StatsActor.Command]
                 ):

  def updateStatus(
                    id: Long,
                    newStatus: ReadingStatus
                  ): ConnectionIO[Option[Book]] =
    for
      oldBook <- repository.findById(id)
      updatedBook <- repository.updateStatus(id, newStatus)
    yield
      oldBook.foreach { book =>
        if book.status != newStatus then
          statsActor ! StatsActor.StatusChanged(
            book.status,
            newStatus
          )
      }

      updatedBook