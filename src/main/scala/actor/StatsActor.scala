package actor

import domain.ReadingStatus
import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

object StatsActor:

  sealed trait Command

  case class StatusChanged(
                            oldStatus: ReadingStatus,
                            newStatus: ReadingStatus
                          ) extends Command

  case class GetStats(
                       replyTo: ActorRef[Stats]
                     ) extends Command

  case class Stats(
                    wantToRead: Int,
                    reading: Int,
                    finished: Int
                  )

  case class Initialize(
                         stats: Stats
                       ) extends Command

  def apply(): Behavior[Command] =
    active(
      wantToRead = 0,
      reading = 0,
      finished = 0
    )

  private def active(
                      wantToRead: Int,
                      reading: Int,
                      finished: Int
                    ): Behavior[Command] =

    Behaviors.receive { (context, message) =>
      message match

        case StatusChanged(oldStatus, newStatus) =>
          context.log.info(
            s"Book status changed: $oldStatus -> $newStatus"
          )

          val updated =
            decrement(oldStatus, wantToRead, reading, finished)

          val finalState =
            increment(newStatus, updated)

          active(
            finalState._1,
            finalState._2,
            finalState._3
          )

        case GetStats(replyTo) =>
          replyTo ! Stats(
            wantToRead,
            reading,
            finished
          )

          Behaviors.same

        case Initialize(stats) =>
          context.log.info(
            s"Stats initialized: $stats"
          )

          active(
            stats.wantToRead,
            stats.reading,
            stats.finished
          )
    }

  private def decrement(
                         status: ReadingStatus,
                         wantToRead: Int,
                         reading: Int,
                         finished: Int
                       ): (Int, Int, Int) =
    status match
      case ReadingStatus.WantToRead =>
        (wantToRead - 1, reading, finished)

      case ReadingStatus.Reading =>
        (wantToRead, reading - 1, finished)

      case ReadingStatus.Finished =>
        (wantToRead, reading, finished - 1)

  private def increment(
                         status: ReadingStatus,
                         state: (Int, Int, Int)
                       ): (Int, Int, Int) =
    val (wantToRead, reading, finished) = state

    status match
      case ReadingStatus.WantToRead =>
        (wantToRead + 1, reading, finished)

      case ReadingStatus.Reading =>
        (wantToRead, reading + 1, finished)

      case ReadingStatus.Finished =>
        (wantToRead, reading, finished + 1)