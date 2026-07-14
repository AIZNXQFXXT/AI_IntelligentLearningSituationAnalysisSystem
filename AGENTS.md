# AI Campus — Agent Guide

## Project

Maven multi-module (Java 17+, Spring Boot 3.2, JavaFX, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for college academic analysis.

```
common-module/   → shared DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups
backend-module/  → Spring Boot REST API on :8080, JWT auth, MyBatis-Plus ORM, AOP logging, No tests
client-module/   → JavaFX/FXML desktop client via OkHttp → backend; **still a skeleton** (CampusApp.java empty)
```

Build **always from root** (`pom.xml` has `<packaging>pom</packaging>`):
```
mvn clean compile -DskipTests
```

## Quick start

```
# 1. Init DB
psql -U test -d ai_campus -f backend-module/src/main/resources/db/init.sql

# 2. Run backend
mvn spring-boot:run -pl backend-module

# 3. App entrypoint
com.campus.backend.CampusApplication
```

Default admin: `admin / 123456`

## Architecture conventions

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus BaseMapper)
- **Soft delete**: MyBatis-Plus `is_deleted` field (0=active, 1=deleted). Never `DELETE FROM`.
- **Entity → DTO**: dedicated `*Converter` classes per domain (no MapStruct).
- **Validation groups**: `Create.class` / `Update.class` on request DTOs.
- **JWT**: access token (2h) + refresh token (7d), custom `JwtAuthInterceptor`.
- **AOP**: `OperationLogAspect` logs every controller method automatically.
- **Pagination**: `PageResult<T>` wrapper, MyBatis-Plus `Page` under the hood.
- **RBAC**: role enum `ADMIN / TEACHER / STUDENT`, role-based menu routing on client side.

## Gotchas

- **Client module is placeholder** — `CampusApp.java` is empty, FXML/styles dirs exist but no real code.
- **No tests anywhere** — no `src/test` in any module.
- **No Maven wrapper** (`mvnw`), no `lombok.config`, no CI/CD.
- **Plaintext secrets in YAML** — DB password and JWT secret committed. Rotate before production.
- **dev/prod YAMLs are identical** — both point to localhost PostgreSQL/Redis. Only `application.yml` is used (no `spring.profiles.active` in config).
- **`application*.yml` is in `.gitignore`** but already tracked — changes ignored after first commit.
- **No AI module code yet** — AI features (diagnosis, comments, risk warning) have tables/services stubs but no DeepSeek integration.
