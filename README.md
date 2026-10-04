# Book Tracker

A small backend-only REST service for tracking books you want to read, are reading, or have finished.
Built as a learning project to get hands-on with Scala 3, Pekko, and a functional-style database layer.

## Features

- CRUD for books (title, author, pages)
- Reading status per book: `WantToRead`, `Reading`, `Finished`
- Live statistics (how many books in each status) kept by a Pekko typed actor
- Versioned database migrations (Flyway)
- Tests: actor tests, repository tests against a real PostgreSQL (Testcontainers), service tests

## Tech stack

| Area | Choice |
|---|---|
| Language | Scala 3 |
| Build | sbt |
| HTTP | Pekko HTTP + spray-json |
| Concurrency | Pekko Actor Typed |
| Database | PostgreSQL 17 |
| DB access | Doobie (cats-effect) + HikariCP |
| Migrations | Flyway |
| Config | Typesafe Config |
| Logging | Logback |
| Tests | ScalaTest, Pekko ActorTestKit, Testcontainers |

## Prerequisites

- JDK 17+
- sbt
- Docker (for the local database and for the tests)

## Getting started

```bash
# 1. start PostgreSQL (exposed on localhost:5433)
docker compose up -d

# 2. run the service (migrations are applied on startup)
sbt run
```

The API is available at `http://localhost:8080`.

### Configuration

`src/main/resources/application.conf`:

```hocon
db {
  url      = "jdbc:postgresql://localhost:5433/book_tracker"
  user     = "postgres"
  password = "postgres"
}
```

The credentials above are for local development only.

## API

| Method | Path | Description |
|---|---|---|
| `GET` | `/books` | List all books |
| `POST` | `/books` | Create a book (starts as `WantToRead`) |
| `GET` | `/books/{id}` | Get one book |
| `PUT` | `/books/{id}` | Update title, author, pages |
| `PATCH` | `/books/{id}/status` | Change reading status |
| `DELETE` | `/books/{id}` | Delete a book |
| `GET` | `/stats` | Number of books per status |

## Project structure

```
src/main/scala
├── BookTrackerApp.scala      # entry point: migrations, actor system, HTTP server
├── actor/StatsActor.scala    # typed actor holding per-status counters
├── api/                      # routes, request DTOs, JSON formats
├── config/                   # AppConfig, Database (transactor), FlywayMigration
├── domain/                   # Book, ReadingStatus (enum)
├── repository/               # BookRepository trait + Doobie implementation
└── service/BookService.scala # status change + notification to the stats actor
src/main/resources
├── application.conf
└── db/migration/             # Flyway migrations (V1, V2)
```

## Tests

```bash
sbt test
```

Docker must be running: the repository test starts a real PostgreSQL container with Testcontainers and applies the Flyway migrations to it.

| Test | What it covers |
|---|---|
| `StatsActorSpec` | initial state and status transitions in the actor |
| `DoobieBookRepositorySpec` | saving and reading a book from a real database |
| `BookServiceSpec` | `StatusChanged` is sent to the actor when the status changes |
