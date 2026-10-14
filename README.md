# RateGuard — Redis-Backed API Rate Limiter

RateGuard is a Java Spring Boot application that limits API requests within a fixed time window. It uses Redis to store request counters and Lua scripting to perform counter updates atomically.

## Features

- Fixed-window rate limiting: 5 requests per client per minute
- Redis Cloud integration
- Atomic counter updates using Redis Lua scripts
- HTTP `429 Too Many Requests` when the limit is exceeded
- `X-RateLimit-Limit` and `X-RateLimit-Remaining` headers
- Dynamic `Retry-After` header
- Automated unit tests

## Tech Stack

- Java 17 compatibility target
- Spring Boot 4
- Spring Web MVC
- Spring Data Redis
- Redis Cloud
- Maven
- JUnit and Mockito

## API Endpoints

| Method | Endpoint          | Description                                   |
| ------ | ----------------- | --------------------------------------------- |
| GET    | `/api/health`     | Checks application health                     |
| GET    | `/api/protected`  | Demonstrates rate limiting                    |
| GET    | `/api/redis-test` | Temporary Redis connectivity test, if enabled |

## Rate-Limiting Behavior

The protected endpoint allows five requests per client within a one-minute fixed window.

| Request  | Expected response       |
| -------- | ----------------------- |
| 1–5      | `200 OK`                |
| 6 onward | `429 Too Many Requests` |

Example response headers:

```http
X-RateLimit-Limit: 5
X-RateLimit-Remaining: 0
Retry-After: 28
```

The `Retry-After` value depends on the remaining lifetime of the Redis counter key.

## Prerequisites

- JDK 17 or later
- Redis Cloud database
- Internet connection for Redis Cloud access

The project includes the Maven Wrapper, so a separate Maven installation is not required.

## Configuration

Set these environment variables before starting the application:

- `REDIS_HOST` — Redis hostname
- `REDIS_PORT` — Redis port
- `REDIS_USERNAME` — Redis username
- `REDIS_PASSWORD` — Redis password

Never commit credentials or secrets to GitHub.

## Run Locally

Configure the required environment variables in PowerShell, then run this command from the project root:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts at `http://localhost:8080`, assuming the port is available.

Test the protected endpoint:

```powershell
Invoke-WebRequest -UseBasicParsing http://localhost:8080/api/protected
```

## Run Tests

Run the automated test suite:

```powershell
.\mvnw.cmd test
```

The unit tests use mocked dependencies. A proper full application-context test and real Redis integration test remain future improvements.

## Current Limitations

- The protected endpoint uses a hardcoded client ID (`student101`).
- Authentication and authorization are not implemented.
- The fixed-window algorithm can allow bursts near window boundaries.
- Redis outages lack dedicated fallback handling.
- Production deployment and load testing remain future improvements.

## Future Improvements

- Identify clients using authenticated user identities or API keys.
- Add robust Redis failure handling.
- Add integration tests using a real or test Redis instance.
- Add metrics, monitoring, and deployment configuration.

## Purpose

RateGuard demonstrates backend API development, Redis-based state management, atomic Lua operations, HTTP response handling, and automated testing.**