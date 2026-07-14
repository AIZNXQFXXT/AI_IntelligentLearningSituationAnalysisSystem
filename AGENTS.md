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

## Async import pipeline

- **POST** `/api/tasks/import-scores` (MultipartFile + examId + courseId) → returns `taskId`
- **GET** `/api/tasks/{id}/progress` — polling
- **GET** `/api/tasks/{id}/result` — final result
- Excel format: columns `学号`, `平时分`, `卷面分`, `最终分`, `缺勤`, `作弊`
- Exam and course selected on upload side, not in Excel
- Thread pool: `importExecutor` (core=2, max=4, queue=10, CallerRunsPolicy)
- Processing: reads temp file → converts rows → creates scores → cleans up temp file
- Error handling: per-row failure doesn't abort the task; errors collected in result JSON
- Missing `TaskService` implementation fixed, `ScoreImportTask.convertRow()` implemented

## Known bugs

- **`MyBatisPlusConfig` auto-fill broken**: `BaseEntity` uses `createdAt`/`updatedAt` but handler sets `"createTime"`/`"updateTime"` (wrong field names) — auto-fill effectively dead
- **CRLF on WSL**: Files on the NTFS mount have `CRLF` line endings; git stores `LF`. After file writes (`Write` tool), run `git add` to convert. Set `core.autocrlf input` if not already.
- **`TaskService` had no impl** (fixed now); `ScoreImportTask` had `convertRow()` as `UnsupportedOperationException` (fixed)

## Gotchas

- **Client module is placeholder** — `CampusApp.java` is empty (0 bytes). No `src/main/resources/`, no FXML, no CSS.
- **Tests exist now** in `backend-module/src/test/java/` (9 unit tests). Run with:
  ```
  mvn test -pl backend-module -am -Dsurefire.failIfNoSpecifiedTests=false
  ```
- **Build**: `mvn clean compile -DskipTests` from root (required for multi-module dependency resolution)
- **No Maven wrapper** (`mvnw`), no `lombok.config`, no CI/CD.
- **Plaintext secrets in YAML** — DB password and JWT secret committed. Rotate before production.
- **`application*.yml` in `.gitignore`** but already tracked — changes ignored after first commit.
- **No AI module code yet** — AI tables/entities exist but no services, controllers, or DeepSeek integration.
