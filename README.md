# Goblin Bank (backend)

Spring Boot API for the Goblin Bank game: house accounts, interest, investments, tradable stocks (house shares + banker-managed item stocks), JWT auth for **banker** and **house** roles, and scheduled backups.

**Stack:** Java 17, Spring Boot 3.5, PostgreSQL, Flyway (schema migrations), JWT, springdoc-openapi (Swagger). Tests use H2 in memory.

---

## Prerequisites

1. **JDK 17** (the Gradle build uses a Java 17 toolchain).
2. **PostgreSQL** (14 or newer is fine; 16+ recommended) running and reachable from your machine.
3. This repository cloned locally.

---

## Setup

The repo is configured for **PostgreSQL on `localhost:5432`**, database `goblin_bank`, user `goblin_bank` (see `spring.datasource.*` in `src/main/resources/application.properties`). **5432** is PostgreSQL’s default TCP port, so this matches a typical local install. If your server listens elsewhere, change the JDBC URL to match.

### 1. Run PostgreSQL

Install PostgreSQL if needed, then start the server. How you start it depends on the install:

- **macOS (Homebrew)** — use the major version you installed, for example:

  ```bash
  brew services start postgresql
  ```

Then - check it's ready

```bash
pg_isready -h localhost -p 5432
```

### 2. Create database role and database

Connect as a PostgreSQL superuser (often `postgres`),

```
psql -h localhost -p 5432 -U "$(whoami)" -d postgres
```

If successful, shell line starts with "postgres=#"

then run:

```sql
CREATE USER goblin_bank WITH PASSWORD 'goblin_bank';
CREATE DATABASE goblin_bank OWNER goblin_bank;
```

### 3. Confirm connectivity

Exit psql
```bash
\q
```

Check the db

```bash
psql -h localhost -p 5432 -U goblin_bank -d goblin_bank -c 'SELECT 1'
```

You should see a single row `1`. If connection is refused, Postgres is not listening on that host/port or authentication failed.

### 4. Run the application

From the project root:

```bash
./gradlew bootRun
```

On first startup, **Flyway** runs SQL in `src/main/resources/db/migration` and applies the schema. Hibernate is set to **validate** only; the database must match migrations.

### 5. Verify the API

With the default Spring Boot port (**8080**):

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Log in as the default banker (after DB is up and seeding runs): see `docs/banker-guide.md` — typically `POST /api/banker/auth/login` with username/password from `goblin.banker.username` / `goblin.banker.password` in `application.properties` (defaults `banker` / `banker`).

### Reset the database (scrub clean)

Use this for a **local dev** wipe so Flyway can recreate everything on the next `bootRun`.

1. **Stop the Spring Boot app** so nothing holds connections to `goblin_bank`.
2. Connect as a **superuser** (same idea as in step 2 above), e.g.:

   ```bash
   psql -h localhost -p 5432 -U "$(whoami)" -d postgres
   ```

3. Drop and recreate the database:

   ```sql
   DROP DATABASE IF EXISTS goblin_bank WITH (FORCE);
   CREATE DATABASE goblin_bank OWNER goblin_bank;
   ```

   `WITH (FORCE)` is supported in **PostgreSQL 13+** and terminates open sessions. On older servers, stop clients first, then use `DROP DATABASE IF EXISTS goblin_bank;` without `WITH (FORCE)`.

4. Run `./gradlew bootRun` again; Flyway reapplies migrations from `src/main/resources/db/migration`.

---

## Other commands

```bash
./gradlew test          # unit/integration tests (H2)
./gradlew bootJar       # build a runnable JAR under build/libs/
```

---

## Configuration (quick reference)

All in `src/main/resources/application.properties` unless you override with env vars or another Spring profile:

| Area | Keys |
|------|------|
| Database | `spring.datasource.url`, `username`, `password` |
| JWT | `goblin.jwt.secret` (use a strong secret, ≥ 32 bytes in production), `goblin.jwt.expiration-seconds` |
| Default banker | `goblin.banker.username`, `goblin.banker.password` |
| Interest bounds | `goblin.rates.min-hourly`, `goblin.rates.max-hourly` |
| Backups | `goblin.backup.*` (JSON dumps under `goblin.backup.dir`) |

---

## Documentation

- `docs/banker-guide.md` — banker endpoints
- `docs/house-stock-guide.md` — house portal
- `docs/goblin-guide.md` — public / house overview
- `docs/frontend-summary.md` — frontend-oriented summary

---

## License

[MIT](LICENSE). Change the copyright line in `LICENSE` if a different legal name should hold it.
