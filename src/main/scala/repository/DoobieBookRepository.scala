package repository

import domain.{Book, ReadingStatus}
import api.CreateBookRequest
import org.typelevel.doobie.*
import org.typelevel.doobie.implicits.*

class DoobieBookRepository extends BookRepository:

  given Meta[ReadingStatus] =
    Meta[String].imap {
      case "WantToRead" => ReadingStatus.WantToRead
      case "Reading" => ReadingStatus.Reading
      case "Finished" => ReadingStatus.Finished
      case other =>
        throw new IllegalArgumentException(s"Unknown reading status: $other")
    } {
      case ReadingStatus.WantToRead => "WantToRead"
      case ReadingStatus.Reading => "Reading"
      case ReadingStatus.Finished => "Finished"
    }

  override def findAll: ConnectionIO[List[Book]] =
    sql"""
      SELECT id, title, author, pages, status
      FROM books
      ORDER BY id
    """.query[Book].to[List]

  override def findById(id: Long): ConnectionIO[Option[Book]] =
    sql"""
      SELECT id, title, author, pages, status
      FROM books
      WHERE id = $id
    """.query[Book].option

  override def save(book: CreateBookRequest): ConnectionIO[Book] =
    sql"""
    INSERT INTO books (title, author, pages, status)
    VALUES (
      ${book.title},
      ${book.author},
      ${book.pages},
      'WantToRead'
    )
    RETURNING id, title, author, pages, status
  """.query[Book].unique

  override def delete(id: Long): ConnectionIO[Int] =
    sql"""
      DELETE FROM books
      WHERE id = $id
    """.update.run

  override def update(
                       id: Long,
                       book: CreateBookRequest
                     ): ConnectionIO[Option[Book]] =
    sql"""
    UPDATE books
    SET title = ${book.title},
        author = ${book.author},
        pages = ${book.pages}
    WHERE id = $id
    RETURNING id, title, author, pages, status
  """.query[Book].option

  override def updateStatus(
                             id: Long,
                             status: ReadingStatus
                           ): ConnectionIO[Option[Book]] =
    sql"""
      UPDATE books
      SET status = $status
      WHERE id = $id
      RETURNING id, title, author, pages, status
    """.query[Book].option