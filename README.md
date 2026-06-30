# Land Registration API

A Spring Boot REST API for registering land plots. The service looks up land records by ID and processes plot registration requests against a PostgreSQL database.

## Tech Stack

- **Java 21**
- **Spring Boot 4.1.0**
- **Spring Data JPA** — persistence layer
- **Spring Web MVC** — REST endpoints
- **PostgreSQL** — database
- **Lombok** — boilerplate reduction
- **Maven** — build and dependency management

## Prerequisites

- Java 21 or later
- Maven 3.6+ (or use the included Maven Wrapper)
- PostgreSQL running locally

## Database Setup

Create a PostgreSQL database before starting the application:

```sql
CREATE DATABASE ardhidb;
```

Default connection settings (see `src/main/resources/application.yaml`):

| Setting  | Value                                      |
|----------|--------------------------------------------|
| URL      | `jdbc:postgresql://localhost:5432/ardhidb` |
| Username | `postgres`                                 |
| Password | `postgres`                                 |

Update `application.yaml` if your local PostgreSQL credentials differ.

## Configuration

Application settings are defined in `src/main/resources/application.yaml`:

```yaml
spring:
  application:
    name: Land-registration
  datasource:
    url: jdbc:postgresql://localhost:5432/ardhidb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: create
    show-sql: true
```

With `ddl-auto: create`, Hibernate recreates the schema on each startup. Use `update` or `validate` in production.

## Getting Started

### Run with Maven Wrapper

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

### Run with Maven

```bash
mvn spring-boot:run
```

### Build the project

```bash
./mvnw clean package
```

### Run tests

```bash
./mvnw test
```

The application starts on the default Spring Boot port **8080**.

## API Reference

Base path: `/api/v1/land`

### Register a land plot

Registers an existing land record by its ID.

**Endpoint:** `POST /api/v1/land/register`

**Request body:**

```json
{
  "landDtoId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Example with curl:**

```bash
curl -X POST http://localhost:8080/api/v1/land/register \
  -H "Content-Type: application/json" \
  -d '{"landDtoId": "550e8400-e29b-41d4-a716-446655440000"}'
```

**Possible responses:**

| Response body                    | Meaning                          |
|----------------------------------|----------------------------------|
| `Land successfully registered`   | Registration completed           |
| `Land not Found!!`               | No land record for the given ID  |
| `Land already registered!!`      | Plot is already registered       |

## Data Model

The `Land` entity (`land` table) includes:

| Field          | Type    | Description                    |
|----------------|---------|--------------------------------|
| `id`           | UUID    | Primary key (auto-generated)   |
| `plotNo`       | String  | Plot number                    |
| `Region`       | String  | Geographic region              |
| `landUse`      | String  | Intended land use              |
| `isRegistered` | boolean | Registration status (default: `false`) |

## Project Structure

```
src/main/java/com/ardhi/Land/registration/
├── LandRegistrationApplication.java   # Application entry point
├── controller/
│   └── LandController.java            # REST endpoints
├── dto/
│   └── RegisterPlotRequestDto.java      # Request payload
├── model/
│   └── Land.java                        # JPA entity
├── repository/
│   └── LandRepository.java              # Data access
└── service/
    ├── RegisterPlotService.java         # Service interface
    └── serviceImpl/
        └── RegisterPlotServiceImpl.java # Registration logic
```

## Repository Queries

`LandRepository` extends `JpaRepository` and provides:

- `findByPlotNo(String plotNo)`
- `findByRegion(String region)`
- `findByPlotNoAndRegion(String plotNo, String region)`
- `findById(UUID id)`

## License

This project is provided as-is. Add license details here if applicable.
