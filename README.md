# Reservation Service: Java, Spring Boot, PostgreSQL, Docker, Testcontainers

## Overview

**Reservation Service** is a REST API for managing reservable resources and time-based reservations.

The application supports resource management, reservation creation and cancellation, search and filtering, pagination, and protection against overlapping reservations. Business rules are enforced at both the application and database levels, including concurrency-safe prevention
of conflicting reservations.

The project uses PostgreSQL for persistence, Flyway for database migrations, Testcontainers for integration testing, and OpenAPI/Swagger for interactive API documentation.

## Key Features

- Create and manage reservable resources
- Deactivate resources that should no longer accept reservations
- Create and cancel reservations
- Prevent reservations for inactive resources
- Prevent overlapping confirmed reservations
- Protect against concurrent reservation conflicts at the database level
- Search resources by name, type, and active status
- Search reservations by resource, status, reserver, and time range
- Combine multiple reservation filters dynamically
- Paginate and sort reservation results
- Manage database schema changes with Flyway migrations
- Test business logic with isolated unit tests
- Run integration tests against PostgreSQL using Testcontainers
- Explore and test the API through Swagger UI
- Run static analysis with PMD and SpotBugs

## API Documentation

When the application is running, interactive API documentation is
available through Swagger UI:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

The generated OpenAPI specification is also available as:

- JSON: `http://localhost:8080/v3/api-docs`
- YAML: `http://localhost:8080/v3/api-docs.yaml`

## Getting Started

### Prerequisites

Make sure the following tools are installed:

- **Java 25**
- **Docker**
- **Docker Compose**

The project includes the Maven Wrapper, so a separate Maven installation is not required.

### Start PostgreSQL

Start the PostgreSQL container with Docker Compose:

```bash
docker compose up -d
```

Verify that the container is running:

```bash
docker compose ps
```

The database schema is managed by Flyway. Migrations are applied automatically when the application starts.

### Run the Application

Start the Spring Boot application using the Maven Wrapper:

```bash
./mvnw spring-boot:run
```

The application will be available at:

```text
http://localhost:8080
```

Interactive API documentation is available through Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

To stop PostgreSQL:

```bash
docker compose down
```

### Verify the Application

Run the automated test suite:

```bash
./mvnw test
```

Run the complete verification lifecycle, including tests and static analysis:

```bash
./mvnw verify
```

The verification build runs the automated test suite together with **PMD** and **SpotBugs** checks.

Integration tests use **Testcontainers** to run against PostgreSQL, allowing persistence behavior, database constraints, migrations, and concurrency scenarios to be tested against the same database technology used by the application.

## API Usage

The API provides endpoints for managing reservable resources and their reservations.

Swagger UI provides the complete interactive API reference. The examples below demonstrate the main workflows.

### Resources

Create a reservable resource:

```bash
curl -X POST http://localhost:8080/api/resources \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Conference Room A",
    "description": "Large conference room",
    "type": "MEETING_ROOM"
  }'
```

List resources:

```bash
curl http://localhost:8080/api/resources
```

Get a resource by ID:

```bash
curl http://localhost:8080/api/resources/1
```

Deactivate a resource:

```bash
curl -X POST http://localhost:8080/api/resources/1/deactivate
```

Resources are active when created. Once a resource is deactivated, new reservations cannot be created for it.

### Reservations

Create a reservation:

```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "reservedBy": "Nancy",
    "startsAt": "2026-08-26T09:00:00Z",
    "endsAt": "2026-08-26T10:00:00Z"
  }'
```

Get a reservation by ID:

```bash
curl http://localhost:8080/api/reservations/1
```

List reservations:

```bash
curl http://localhost:8080/api/reservations
```

Cancel a reservation:

```bash
curl -X POST http://localhost:8080/api/reservations/1/cancel
```

Reservations are created with the `CONFIRMED` status. Cancelling a reservation changes its status to `CANCELLED` and makes its time range available for a new reservation.

Confirmed reservations for the same resource cannot overlap.

### Filtering, Pagination, and Sorting

Resources can be filtered by type, active status, and name.

Filter resources by type:

```bash
curl "http://localhost:8080/api/resources?type=MEETING_ROOM"
```

Filter active resources:

```bash
curl "http://localhost:8080/api/resources?active=true"
```

Search resources by name:

```bash
curl "http://localhost:8080/api/resources?name=conference"
```

Resource name matching is case-insensitive and supports partial matches. Filters can also be combined:

```bash
curl "http://localhost:8080/api/resources?type=MEETING_ROOM&active=true"
```

Reservations can be filtered by resource, status, reserver, and overlapping time range.

Filter confirmed reservations:

```bash
curl "http://localhost:8080/api/reservations?status=CONFIRMED"
```

Filter reservations for a specific resource:

```bash
curl "http://localhost:8080/api/reservations?resourceId=1"
```

Search by reserver:

```bash
curl "http://localhost:8080/api/reservations?reservedBy=nancy"
```

Search for reservations overlapping a time range:

```bash
curl "http://localhost:8080/api/reservations?from=2026-08-26T09:30:00Z&to=2026-08-26T10:30:00Z"
```

Reservation filters can be combined:

```bash
curl "http://localhost:8080/api/reservations?resourceId=1&status=CONFIRMED"
```

Reservation results support pagination:

```bash
curl "http://localhost:8080/api/reservations?page=0&size=10"
```

They can also be sorted using Spring Data sorting parameters:

```bash
curl "http://localhost:8080/api/reservations?sort=startsAt,asc"
```

Pagination, sorting, and filtering can be combined in the same request:

```bash
curl "http://localhost:8080/api/reservations?status=CONFIRMED&page=0&size=10&sort=startsAt,asc"
```

The reservation endpoint returns paginated results together with metadata such as the current page, page size, total number of elements, and total number of pages.

## How It Works

### Architecture

The application follows a layered structure that separates HTTP handling, business logic, and persistence:

```text
HTTP Request
     │
     ▼
Controller
     │
     ▼
Service
     │
     ▼
Repository / Specifications
     │
     ▼
PostgreSQL
```

- **Controllers** expose the REST API and handle request validation.
- **Services** enforce business rules and coordinate application operations.
- **Repositories and Specifications** provide persistence and dynamic filtering.
- **PostgreSQL** stores application data and provides database-level integrity guarantees.
- **Flyway** manages versioned database schema migrations.

## Business Rules

### Resource Lifecycle

Resources are active when created. An inactive resource cannot accept new reservations, and an already inactive resource cannot be deactivated again.

### Reservation Lifecycle

Reservations are created with `CONFIRMED` status and can later be cancelled. Cancelling a reservation releases its time range for future reservations. An already cancelled reservation cannot be cancelled again.

### Overlapping Reservations

Confirmed reservations for the same resource cannot overlap. Adjacent reservations are allowed, so `09:00–10:00` and `10:00–11:00` do not conflict.

### Concurrency Protection

The service checks for overlapping reservations before creation to provide an immediate conflict response.

PostgreSQL also enforces the overlap constraint at the database level, preventing concurrent requests from creating conflicting confirmed reservations if they pass the application-level check at the same time.

## Database and Migrations

The application uses **PostgreSQL** for persistence and **Flyway** for versioned database migrations.

Migrations are stored in `src/main/resources/db/migration` and are applied automatically when the application starts.

Database constraints complement application-level validation, including protection against overlapping confirmed reservations.

## Development and Testing

Run the test suite with:

```bash
./mvnw test
```

Run the complete verification lifecycle:

```bash
./mvnw verify
```

The project includes unit and integration tests covering resource and reservation lifecycles, validation, filtering, pagination, conflict handling, and concurrent reservation attempts.

Integration tests use **Testcontainers** with PostgreSQL, while **PMD** and **SpotBugs** provide static analysis as part of the Maven verification lifecycle.

## Tech Stack

| Technology | Purpose |
| --- | --- |
| **Java 25** | Core application language |
| **Spring Boot** | Application framework |
| **Spring Web MVC** | REST API |
| **Spring Data JPA** | Persistence and dynamic query specifications |
| **PostgreSQL** | Relational database |
| **Flyway** | Database migrations |
| **Docker Compose** | Local database environment |
| **Testcontainers** | PostgreSQL integration testing |
| **JUnit 5** | Automated testing |
| **Mockito** | Unit-test mocking |
| **AssertJ** | Fluent test assertions |
| **OpenAPI / Swagger UI** | Interactive API documentation |
| **PMD** | Static code analysis |
| **SpotBugs** | Bug-pattern analysis |
| **Maven** | Build and dependency management |

## Project Status and Future Ideas

Reservation Service is a portfolio-focused project demonstrating a production-style REST API with business rules, PostgreSQL persistence, concurrency protection, database migrations, automated testing, and API documentation.

Possible future extensions include:

- **Authentication and authorization** — Add user accounts and role-based access to resource and reservation operations.
- **Reservation rescheduling** — Allow existing reservations to be moved while preserving overlap protection.
- **Availability queries** — Provide dedicated endpoints for finding available resources within a requested time range.
- **Observability** — Add application metrics, health monitoring, and structured logging.
- **CI/CD** — Automate verification and deployment through a continuous integration pipeline.

The current version intentionally focuses on the core reservation domain and its consistency guarantees rather than expanding into user management or scheduling UI concerns.
