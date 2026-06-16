# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run locally
./gradlew bootRun

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.video.backend.video_backend.VideoBackendApplicationTests"

# Clean build
./gradlew clean build
```

## Configuration

`application.yaml` is gitignored. You must create it locally with:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/<db>
    username: <user>
    password: <pass>
  jpa:
    hibernate:
      ddl-auto: update

app:
  media:
    base-path: /path/to/media   # videos go to {base-path}/video/, thumbnails to {base-path}/thumbnails/
  cors:
    allowed-origins:
      - http://localhost:3000

jwt:
  secret: <secret-key>
```

## Architecture

Standard Spring Boot layered architecture: **Controller → Service → Repository → Entity**.

- `controller/` — REST endpoints
- `service/` — business logic
- `repository/` — Spring Data JPA interfaces
- `entity/` — JPA entities (6 tables: `User`, `Video`, `Tag`, `VideoTagTemporal`, `VideoDuplicateGroup`, `VideoDuplicateMember`)
- `dto/` — request/response objects
- `mapper/` — MapStruct mappers (entity ↔ DTO)
- `security/` — JWT cookie-based auth (`JwtAuthenticationFilter`, `JwtService`, `SecurityConfig`)
- `excepcion/` — `GlobalExceptionHandler` + domain exceptions

## Key Domain Concepts

**Video streaming** — `VideoService.findVideoById()` supports HTTP Range requests, streaming in 1 MB chunks. FFmpeg is used to auto-generate thumbnails from the 2-second mark when none is provided.

**Tag system** — Two layers: permanent tags (`Tag`/`VideoTag` many-to-many) and `VideoTagTemporal` which holds AI-suggested tags pending user confirmation. `VideoTagTemporalController` exposes the pending queue.

**Duplicate detection** — `VideoDuplicateGroup` and `VideoDuplicateMember` track groups of similar videos with a similarity score. Members have an `Accion` enum state: `PENDIENTE`, `ES_DUPLICADO`, `NO_ES_DUPLICADO`.

**Multi-tag search** — `GET /api/videos/tag` with multiple tag params uses AND logic (video must have all specified tags).

## Security

JWT tokens are issued at login, stored in HTTP-only secure cookies, and validated per request. Only `/api/auth/login` is public. Sessions are stateless; CSRF is disabled.
