# AI Workshop Demo

Project contains two Spring Boot services:

- member-service
- redemption-service

## Run with Docker

Prerequisite: Docker Desktop (or Docker Engine with Compose).

1. Optional: copy `.env.example` to `.env` and change the database credentials or exposed ports.
2. Build and start PostgreSQL and both services:

   ```bash
   docker compose up --build
   ```

3. The services are available at:

   - Member service: `http://localhost:8061`
   - Redemption service: `http://localhost:8062`
   - PostgreSQL: `localhost:5432`

PostgreSQL executes `database/workshop-postgresql.sql` automatically when the
`postgres-data` volume is created for the first time.

Stop the containers with `docker compose down`. To also remove the database
volume and run the initialization script again, use `docker compose down -v`.

## Run without Docker

Prerequisites:

- JDK 21 (both services target Java 21)
- Maven 3.9+
- PostgreSQL 15+

Database:

- URL: `jdbc:postgresql://localhost:5432/demoDb`
- Username: `postgres`
- Password: `postgres`

Setup:

1. Create database `demoDb` in PostgreSQL.
2. Execute `database/workshop-postgresql.sql`.
3. Build all services:
   `mvn clean package`
4. Start member-service:
   `mvn -f member-service/pom.xml spring-boot:run`
5. Start redemption-service:
   `mvn -f redemption-service/pom.xml spring-boot:run`
