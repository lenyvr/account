# Account Microservice

A bank account management microservice built with Java 21 and Spring Boot 3, following **Hexagonal Architecture** principles. It is part of a fintech ecosystem and communicates asynchronously with other services via RabbitMQ.

## Tech Stack

| Component         | Technology              |
|-------------------|-------------------------|
| Language          | Java 21                 |
| Framework         | Spring Boot 3.5.14      |
| Build             | Gradle 8.14 (Kotlin DSL)|
| Database          | PostgreSQL 18           |
| Messaging         | RabbitMQ 3.13           |
| API Documentation | SpringDoc OpenAPI 2.8.9 |
| ORM               | Spring Data JPA         |
| Containers        | Docker + Docker Compose |

## Architecture

The project is split into three Gradle modules that map to the layers of hexagonal architecture:

```
domain/          → Business entities, value objects, ports, and domain exceptions
                   No external dependencies (no Spring, no JPA)

application/     → Use cases as pure Java POJOs
                   No Spring annotations; constructor injection only
                   Depends on: domain

infrastructure/  → Spring Boot, REST controllers, JPA repositories,
                   RabbitMQ listeners, and bean configuration
                   Depends on: domain, application
```

## Project Structure

```
account/
├── domain/
│   └── src/main/java/
├── application/
│   └── src/main/java/
├── infrastructure/
│   ├── src/main/java/
│   │   └── AccountApplication.java
│   └── src/main/resources/
│       ├── application.yaml
│       ├── schema.sql
│       └── data.sql
├── Dockerfile
├── docker-compose.yml
├── .env
├── build.gradle.kts
└── settings.gradle.kts
```

## Prerequisites

- Docker and Docker Compose
- Java 21 (only for local development without Docker)

## Environment Variables

Create a `.env` file at the project root with the following variables:

```env
DB_NAME=
DB_USER=
DB_PASSWORD=
APP_PORT=8080
RBQ_HOST=
RBQ_PORT=5672
RBQ_USER=
RBQ_PASSWORD=
```

## Running the Environment

```bash
# Start all services (app + database + RabbitMQ)
docker-compose up --build

# Database only
docker-compose up devsu-account-db

# Messaging only: You must verify that the `client` service has not already opened the messaging queue to avoid conflicts.
docker-compose up devsu-fintech-queue
```

## Services and Ports

| Service           | External Port | Description                   |
|-------------------|---------------|-------------------------------|
| Spring Boot App   | `8081`        | Application REST API          |
| PostgreSQL        | `5433`        | Main database                 |
| RabbitMQ AMQP     | `5672`        | Message broker                |
| RabbitMQ Manager  | `15672`       | RabbitMQ management UI        |

## API Documentation

With the application running, access the Swagger UI at:

```
http://localhost:8081/swagger-ui.html
```

## Data Model

### Core Tables

- **account** — Bank accounts (savings, checking, time deposit)
- **transaction** — Financial movements associated with an account

### Parameter Tables

| Table              | Values                                                              |
|--------------------|---------------------------------------------------------------------|
| `account_type`     | SAVINGS, CHECKING, TIME_DEPOSIT                                     |
| `account_status`   | PENDING_ACTIVATION, ACTIVE, BLOCKED, DORMANT, CLOSED               |
| `transaction_type` | CASH_DEPOSIT, TRANSFER_INBOUND, CASH_WITHDRAWAL, TRANSFER_OUTBOUND |

The schema is automatically initialized via `schema.sql` and `data.sql` on application startup.

## Use Cases

- **Create account** — With client verification via RabbitMQ RPC
- **Update account** — Status changes and expiry date updates
- **List accounts** — Paginated, filterable by number, type, status, dates, and balance range
- **Deactivate account** — Transition to CLOSED status with remaining balance handling
- **Register transaction** — Deposits, withdrawals, and transfers with balance update
- **Account report** — Account statement by client and period

## Asynchronous Messaging (RabbitMQ)

| Element               | Value                          |
|-----------------------|--------------------------------|
| Exchange              | `accounts.exchange`            |
| Request queue (RPC)   | `accounts.check-request`       |
| Response queue        | `client.deactivation-response` |

## Development Conventions

- **Dependency injection:** constructor-only, no field-level `@Autowired`
- **DTOs:** prefer Java `record` for immutability
- **Output port naming:** `[Name]RepositorySPI`
- **Use case naming:** `[Action][Entity]UseCase`
- **Commits:** Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`, `chore:`)
- **Branches:** `feature/TASK-##-description` or `bugfix/TASK-##-description`

## Build

```bash
./gradlew clean build
```
