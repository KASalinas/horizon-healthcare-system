# Horizon Healthcare Information System

Horizon Milestone 3 is a Java 21 and Spring Boot 3.5 REST API for patient and
encounter management. Patient data is stored in MySQL. Automated tests use an
in-memory H2 database, with optional real-MySQL coverage through Testcontainers.

The original dependency-free CLI remains available during the migration.

## Requirements

- Java 21
- MySQL 8 or newer for running the API

The included Maven Wrapper downloads the pinned Maven version on first use.

## Database setup

Create an empty MySQL database and provide connection settings through
environment variables. Credentials are never stored in this repository.

```sh
export DB_URL='jdbc:mysql://localhost:3306/horizon'
export DB_USERNAME='horizon'
export DB_PASSWORD='replace-with-a-local-secret'
```

Flyway applies versioned schema migrations automatically. Hibernate validates
the resulting schema but does not create or update it. `DB_BASELINE_ON_MIGRATE`
is optional and defaults to `false`; only enable it deliberately when adopting
an existing non-empty schema.

## Build and test

Run the Spring Boot API and persistence tests:

```sh
./mvnw clean test
```

When Docker is available, the suite also runs its MySQL Testcontainers test.
Without Docker, that single test is skipped and the H2 suite still runs.

Run the preserved CLI tests:

```sh
./build.sh
```

## Run

Start the REST API:

```sh
./mvnw spring-boot:run
```

Start the legacy in-memory CLI:

```sh
./build.sh run
```

## REST API

JSON dates use strict ISO format: `YYYY-MM-DD`.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/patients` | Register a patient |
| `GET` | `/api/patients/{mrn}` | Find a patient |
| `GET` | `/api/patients` | List patients |
| `PUT` | `/api/patients/{mrn}` | Update a patient |
| `PATCH` | `/api/patients/{mrn}` | Partially update a patient |
| `POST` | `/api/patients/{mrn}/encounters` | Record an encounter |
| `GET` | `/api/patients/{mrn}/encounters` | List encounters |
| `GET` | `/api/patients/{mrn}/encounters/{id}` | Get an encounter |

Patient listing accepts `page`, `size`, `sort`, and `direction` query
parameters. Page size is limited to 100 and sortable fields are allow-listed.

Interactive OpenAPI documentation is available at `http://localhost:8080/docs`;
the JSON specification is available at `http://localhost:8080/v3/api-docs`.

Example registration:

```sh
curl -X POST http://localhost:8080/api/patients \
  -H 'Content-Type: application/json' \
  -d '{"medicalRecordNumber":"MRN-1001","fullName":"Taylor Morgan","dateOfBirth":"1990-06-20","email":"taylor@example.test"}'
```

Authentication and a web interface are intentionally deferred.
