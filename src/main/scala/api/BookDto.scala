package api

import domain.ReadingStatus

final case class CreateBookRequest(
                                    title: String,
                                    author: String,
                                    pages: Int
                                  )

final case class BookResponse(
                               id: Long,
                               title: String,
                               author: String,
                               pages: Int
                             )

final case class UpdateBookStatusRequest(
                                          status: ReadingStatus
                                        )