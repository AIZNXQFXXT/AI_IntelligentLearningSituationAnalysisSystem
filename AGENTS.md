# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2, JavaFX, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups
backend-module/  Spring Boot REST API :8080, JWT auth, MyBatis-Plus ORM, AOP logging
client-module/   JavaFX/FXML desktop client (multi-module build, older/less complete)
```

**`javafx-frontend/`** — standalone JavaFX project (NOT in root pom.xml, separate `pom.xml` + own Maven wrapper). Tracked by git. Has its own `build.sh` that embeds a Vue.js build into resources for WebView screens. Run with `mvn javafx:run -f javafx-frontend/pom.xml`.

## Build & Run

```bash
# compile everything
mvn compile -DskipTests

# run backend (JAVA_HOME must be set on Linux)
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true

# single test class
mvn test -pl backend-module -Dtest=UserControllerTest -DfailIfNoTests=false

# run backend-module tests (including common-module compilation)
mvn test -pl backend-module -am -Dtest=SystemControllerTest,SystemServiceImplTest -DfailIfNoTests=false
```

> **CRITICAL**: Adding/editing VO/DTO classes in `common-module` requires installing the JAR:
> ```bash
> mvn install -DskipTests -pl common-module -am
> ```
> Without this, `spring-boot:run -pl backend-module` fails with `ClassNotFoundException`.
> Tests with `-pl backend-module` resolve common-module from `target/classes` — re-compilation suffices.

Default admin: `admin / 123456`

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`)
- **Entity → DTO**: manual `*Converter` per domain, no MapStruct
- **Validation groups**: `@Validated(Create.class)` / `@Validated(Update.class)` on request DTOs (not `@Valid`)
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`.
- **JWT**: access token (2h) + refresh token (7d), custom `JwtAuthInterceptor` on `/api/**`. Login: `username` + `password`. Whitelisted: `/api/auth/login`, `/api/auth/refresh`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`
- **Pagination**: `PageResult.of(records, total, page, size)` from MyBatis-Plus `Page`
- **RBAC**: ADMIN / TEACHER / STUDENT, role-based menu routing, `SecurityHelper.requireAdmin(request)` for admin-only endpoints
- **All entities** extend `BaseEntity` (id auto, createdAt, updatedAt, isDeleted)

## Testing (pure JUnit + Mockito, no Spring test context)

`@WebMvcTest` / `@SpringBootTest` **will fail** — `@MapperScan` on `CampusApplication` forces DataSource dependency. All tests use standalone MockMvc setup:

```java
MockMvcBuilders.standaloneSetup(controller)
    .setControllerAdvice(new GlobalExceptionHandler())
    .setValidator(new LocalValidatorFactoryBean())
    .build();
```

Test files exist on disk but **are NOT tracked by git** (`.gitignore` has `test/`). Existing tests: `UserControllerTest`, `SystemControllerTest`, `LogControllerTest`, `StudentServiceImplTest`, `TaskServiceImplTest`, `AcademicStatsControllerTest`, `AcademicStatsServiceImplTest`, `GenericTaskControllerTest`, `AsyncConfigTest`.

## Async Import Pipeline

### Domain-specific endpoints
- `POST /api/{domain}/batch` (MultipartFile) → `{taskId}`
- `GET /api/scores/{taskId}/progress` / `.../result` — polling

### Generic unified endpoint (`GenericTaskController`)
- `POST /api/tasks?type=SCORE_IMPORT&examId=&courseId=` (MultipartFile) → `{taskId, status}`
- `GET /api/tasks/{taskId}` — progress; `GET /api/tasks/{taskId}/result` — result
- Params passed as request params (not body); `teacherId` injected from request attribute
- `TaskHandler` interface: each handler registers via `getType()` and creates `Runnable` via `createRunnable()`

### Import task patterns (must follow exactly)

- `@AllArgsConstructor`
- Fields: `taskId`, `fileUrl`, domain service, `taskService`
- Parse: `Path.of(URI.create(fileUrl))` — NOT string replace (breaks on Windows `file:///C:/...`)
- Catch: `catch(Throwable)` — not just Exception
- Progress: `10 + (i+1)*80/size` every 50 rows
- Cleanup: delete temp file in `finally`
- `ObjectMapper` instantiated per invocation (no shared state)
- `@Transactional(propagation = REQUIRES_NEW)` on `TaskServiceImpl` methods called from background threads

### Thread pool
`importExecutor` (core=2, max=4, queue=10, `CallerRunsPolicy`). Per-row failure doesn't abort; errors collected in result JSON.

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` fills `"createTime"`/`"updateTime"` — set timestamps manually in service code.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok doesn't copy `@Qualifier`. Write a manual constructor (see `TaskController` / `TeacherController` / `StudentController`).
- **`.gitignore` traps**: `*.yml` (application config not tracked), `.xlsx` (import templates not tracked), `test/` (test files not committed), `docs/` (documentation not committed), `*.log`.
- **application config**: `application.yml`, `application-dev.yml`, `application-prod.yml` all exist on disk but are gitignored. With `spring.profiles.active=dev` (or `prod`), the dev/prod overrides merge with defaults in `application.yml`.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **JAVA_HOME**: On Linux, `mvn spring-boot:run` fails without it. Use `export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))` (JDK 17).
- **No AI implementation**: `ai/` package is empty — no DeepSeek integration yet.
- **`Map<String, Integer>` for status**: `PUT /{id}/status` endpoints accept `?status=1` query param, not JSON body.
- **Two frontends**: `client-module` (multi-module, less complete) and `javafx-frontend` (standalone, tracked, hybrid FXML + WebView/Vue).
