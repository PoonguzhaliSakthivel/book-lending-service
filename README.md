# Book Lending Service

A small Spring Boot service for running a library's lending desk: a book catalog, member
records, and borrow/return tracking with configurable loan rules (max active loans per
member, loan duration, etc.).

It's built with Java 21, Spring Boot 3.3, Spring Data JPA, Spring Security, and H2 (file-based,
so data survives restarts). Flyway handles the schema.

## Running it

```bash
./mvnw spring-boot:run
```

The app comes up on `http://localhost:8080`. Swagger UI is at `/swagger-ui.html` and the
OpenAPI spec at `/v3/api-docs`, both open without auth. A snapshot of the generated spec is
also checked in at [doc/swagger.json](doc/swagger.json).

Two users are seeded in memory for basic auth:

| username    | password       | role      |
|-------------|----------------|-----------|
| `librarian` | `librarian123` | LIBRARIAN |
| `member`    | `member123`    | MEMBER    |

Member management (`/api/members/**`) and the actuator endpoints beyond `health`/`info`
require the `LIBRARIAN` role. Books and loans just need to be authenticated as either user.

A Postman collection ([doc/book-lending-service.postman_collection.json](doc/book-lending-service.postman_collection.json))
is included if you'd rather click through the API than curl it.

## API at a glance

**Books** — `/api/books`
- `POST /api/books` — add a book
- `GET /api/books` — search/paginate (`?q=`)
- `GET /api/books/{id}`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`

**Members** — `/api/members` (librarian only)
- `POST /api/members`
- `GET /api/members`
- `GET /api/members/{id}`

**Loans** — `/api/loans`
- `POST /api/loans/borrow` — check out a book to a member
- `POST /api/loans/{id}/return` — return it
- `GET /api/loans/{id}`
- `GET /api/loans`, `/api/loans/active`, `/api/loans/overdue`
- `GET /api/loans/member/{memberId}`

Borrowing enforces a few things automatically: no borrowing past the configured active-loan
limit, no duplicate loans on a book that's already out, and no copies going negative.

## Configuration

The loan rules live under `library.loan` in `application.yaml`:

```yaml
library:
  loan:
    max-active-loans-per-member: 3
    loan-duration-days: 14
```

## Data

H2 runs in file mode (`./data/book-lending`), so the database persists between runs. The H2
console is enabled at `/h2-console` if you want to poke around directly. Schema changes go
through Flyway migrations in `src/main/resources/db/migration`.

## Tests

```bash
./mvnw test
```

Covers repositories, services, controllers, a full borrow/return integration flow, and a
concurrency test around simultaneous loan attempts on the same book.
