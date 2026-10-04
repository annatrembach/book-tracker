package repository

import api.CreateBookRequest
import domain.{Book, ReadingStatus}
import org.typelevel.doobie.ConnectionIO

trait BookRepository:

  def findAll: ConnectionIO[List[Book]]

  def findById(id: Long): ConnectionIO[Option[Book]]

  def save(book: CreateBookRequest): ConnectionIO[Book]

  def delete(id: Long): ConnectionIO[Int]

  def update(id: Long, book: CreateBookRequest): ConnectionIO[Option[Book]]

  def updateStatus(id: Long, status: ReadingStatus): ConnectionIO[Option[Book]]