# Goblin Bank (backend)

Spring Boot API for the Goblin Bank game: house accounts, interest, investments, tradable stocks (house shares + banker-managed item stocks), JWT auth for **banker** and **house** roles, and scheduled backups.

## Stack

- **Java** 17, **Spring Boot** 3.5
- **Database: PostgreSQL** — schema owned by **Flyway** (`src/main/resources/db/migration`)
- **JPA/Hibernate** with `ddl-auto=validate` (migrations are the source of truth)
- **JWT** (HS256) for API auth
- **springdoc-openapi** — Swagger UI at `/swagger-ui.html`, OpenAPI JSON at `/v3/api-docs`
- Tests use **H2** in-memory (`build.gradle`)

## Database

Default connection (see `src/main/resources/application.properties`):

| Setting | Value |
|--------|--------|
| JDBC URL | `jdbc:postgresql://localhost:5433/goblin_bank` |
| User / password | `goblin_bank` / `goblin_bank` |

Create a matching database and user before first run, or point `spring.datasource.*` at your own instance. Example with Docker:

```bash
docker run --name goblin-bank-db -e POSTGRES_USER=goblin_bank \
  -e POSTGRES_PASSWORD=goblin_bank -e POSTGRES_DB=goblin_bank \
  -p 5433:5432 -d postgres:16
```

On startup, Flyway applies migrations; the app will fail fast if the schema does not match entities.

## Run locally

Prerequisites: **JDK 17**, **PostgreSQL** reachable with the configured URL.

```bash
./gradlew bootRun
```

Other useful commands:

```bash
./gradlew test
./gradlew bootJar
```

## Configuration

Important keys in `application.properties`:

- **Datasource** — `spring.datasource.url`, `username`, `password`
- **JWT** — `goblin.jwt.secret` (use a strong secret ≥ 32 bytes in production), `goblin.jwt.expiration-seconds`
- **Default banker login** — `goblin.banker.username`, `goblin.banker.password` (seeded if missing)
- **Interest bounds** — `goblin.rates.min-hourly`, `goblin.rates.max-hourly`
- **Backups** — `goblin.backup.*` (JSON dumps under `goblin.backup.dir`)

Override any of these via environment variables or a profile-specific file as needed for your environment.

## Documentation

API and role behavior are described under `docs/`:

- `docs/banker-guide.md` — banker endpoints (accounts, interest, investments, pawn shop, share-price tuning, game clock)
- `docs/house-stock-guide.md` — house portal usage
- `docs/goblin-guide.md` — public / house-facing overview
- `docs/frontend-summary.md` — quick reference for a frontend

## License

[MIT](LICENSE). 

