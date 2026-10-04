package api

import actor.StatsActor
import domain.{Book, ReadingStatus}
import spray.json.*

object JsonFormats extends DefaultJsonProtocol:

  given RootJsonFormat[ReadingStatus] with

    def write(status: ReadingStatus): JsValue =
      JsString(status.toString)

    def read(json: JsValue): ReadingStatus =
      json match
        case JsString("WantToRead") => ReadingStatus.WantToRead
        case JsString("Reading")    => ReadingStatus.Reading
        case JsString("Finished")   => ReadingStatus.Finished
        case _ =>
          deserializationError("Invalid reading status")

  given bookFormat: RootJsonFormat[Book] =
    jsonFormat5(Book.apply)

  given createBookRequestFormat: RootJsonFormat[CreateBookRequest] =
    jsonFormat3(CreateBookRequest.apply)

  given bookResponseFormat: RootJsonFormat[BookResponse] =
    jsonFormat4(BookResponse.apply)

  given updateBookStatusRequestFormat: RootJsonFormat[UpdateBookStatusRequest] =
    jsonFormat1(UpdateBookStatusRequest.apply)

  given statsFormat: RootJsonFormat[StatsActor.Stats] =
    jsonFormat3(StatsActor.Stats.apply)