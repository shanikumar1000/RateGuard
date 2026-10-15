
# RateGuard — Redis-Backed API Rate Limiter

RateGuard is a Java Spring Boot application that limits API requests within a fixed time window. It uses Redis to store request counters and Lua scripting to perform counter updates atomically.

## Features

- Fixed-window rate limiting: 5 requests per client per minute
- Redis Cloud integration
- Atomic counter updates using Redis Lua scripts
- API-key authentication using the `X-API-Key` header
- Client identification using the `X-Client-Id` header
- HTTP `401 Unauthorized` for missing or invalid API keys
- HTTP `429 Too Many Requests` when the rate limit is exceeded
- `X-RateLimit-Limit` and `X-RateLimit-Remaining` response headers
- `Retry-After` and `X-RateLimit-Reset` headers when the limit is exceeded
- Health checks and monitoring with Spring Boot Actuator
- Automated unit tests using JUnit and Mockito

## Tech Stack

- Java 17 compatibility target
- Spring Boot 4
- Spring Web MVC
- Spring Data Redis
- Redis Cloud
- Spring Boot Actuator
- Maven Wrapper
- JUnit and Mockito

## API Endpoints

| Method | Endpoint                          | Description                                           | Authentication                            |
| ------ | --------------------------------- | ----------------------------------------------------- | ----------------------------------------- |
| GET    | `/api/health`                     | Application and Redis health check                    | Not required                              |
| GET    | `/api/protected`                  | Demonstrates API-key authentication and rate limiting | Required                                  |
| GET    | `/actuator/health`                | Actuator health information                           | Not required by the current configuration |
| GET    | `/actuator/metrics`               | Lists available application metrics                   | Not required by the current configuration |
| GET    | `/actuator/metrics/{metric.name}` | Retrieves a specific metric                           | Not required by the current configuration |

**Note:** Actuator endpoint exposure and access controls depend on the application's configuration. The current configuration exposes health, info, and metrics endpoints.

## Rate-Limiting Behavior

The protected endpoint allows five requests per client within a one-minute fixed window.

| Request                                  | Expected response                       |
| ---------------------------------------- | --------------------------------------- |
| Requests 1–5                             | `200 OK`, assuming valid authentication |
| Request 6 onward, within the same window | `429 Too Many Requests`                 |

Requests are counted separately for each client ID. The Redis Lua script increments the counter atomically and sets its expiration when the counter is first created.

Example response headers when the limit is reached:

```http
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
Retry-After: 28
X-RateLimit-Reset: <unix-timestamp>
```

The `Retry-After` value indicates the approximate number of seconds until the current rate-limit window expires. Its value depends on the remaining lifetime of the Redis counter key.

The fixed-window approach can allow bursts near window boundaries.

## Prerequisites

- JDK 17 or later
- A Redis Cloud database or another compatible Redis instance
- Internet connection if using Redis Cloud

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Configuration

Set the following environment variables before starting the application:

| Variable            | Purpose                                 |
| ------------------- | --------------------------------------- |
| `REDIS_HOST`        | Redis hostname                          |
| `REDIS_PORT`        | Redis port                              |
| `REDIS_USERNAME`    | Redis username                          |
| `REDIS_PASSWORD`    | Redis password                          |
| `RATEGUARD_API_KEY` | Expected API key for protected requests |

The application reads these values through `application.properties`.

Never commit actual credentials or API keys to GitHub. Use your own secret values and keep them out of source control.

## Run Locally — Windows PowerShell

Open a terminal in the project root.

Set your environment variables using your own Redis configuration:

```powershell
$env:REDIS_HOST = "your-redis-host"
$env:REDIS_PORT = "your-redis-port"
$env:REDIS_USERNAME = "default"
$env:REDIS_PASSWORD = "your-redis-password"
$env:RATEGUARD_API_KEY = "your-api-key"
```

Start the application:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at `http://localhost:8080`, assuming the port is available.

Environment variables set this way apply to the current PowerShell session.

## Test the Health Endpoint

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

The response should indicate whether the application is up and Redis is connected.

You can also check Actuator health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Test the Protected Endpoint

Replace the example API key with the same value configured in `RATEGUARD_API_KEY`:

```powershell
Invoke-RestMethod `
    -Uri "http://localhost:8080/api/protected" `
    -Headers @{
        "X-Client-Id" = "clientA"
        "X-API-Key" = "your-api-key"
    }
```

A valid request under the rate limit should return a successful response.

- Missing or invalid API key: `401 Unauthorized`
- Invalid client ID: `400 Bad Request`
- Rate limit exceeded: `429 Too Many Requests`

## Monitoring with Actuator

List available metrics:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/metrics
```

Inspect JVM memory usage:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/metrics/jvm.memory.used
```

The JVM memory metric reports memory usage in bytes.

**Security note:** Actuator endpoints can expose operational information. Before public deployment, restrict access to these endpoints and avoid exposing sensitive health details or metrics unnecessarily.

## Run Tests

Run the automated test suite:

```powershell
.\mvnw.cmd test
```

The unit tests use mocked dependencies to verify controller behavior, authentication, rate-limit responses, and Redis-related service logic.

A real Redis integration test and a full application-context test remain possible future improvements.

## Current Limitations

- The rate limiter uses a fixed one-minute window.
- Redis outages can cause protected requests to fail; production-grade fallback behavior is not implemented.
- The API-key authentication mechanism is a simple shared-key check, not a complete user authentication and authorization system.
- API keys are not individually managed or associated with separate quotas.
- Actuator access controls should be reviewed before public deployment.
- Production deployment and load testing remain future improvements.

## Future Improvements

- Add integration tests using a real or test Redis instance.
- Add production-grade Redis failure handling.
- Introduce configurable per-client limits and window durations.
- Improve API-key management and secret rotation.
- Secure monitoring endpoints for deployment.
- Deploy the application and conduct load testing.

## Purpose

RateGuard demonstrates backend API development, Redis-based state management, atomic Lua operations, API-key authentication, HTTP response handling, health monitoring, metrics, and automated testing.
