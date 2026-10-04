package domain

final case class Book(
                       id: Long,
                       title: String,
                       author: String,
                       pages: Int,
                       status: ReadingStatus
                     )