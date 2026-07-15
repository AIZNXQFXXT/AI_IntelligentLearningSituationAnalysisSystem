# AI Campus — Agent Guide

Maven multi-module (Java 17+, Spring Boot 3.2, JavaFX, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups
backend-module/  Spring Boot REST API :8080, JWT auth, MyBatis-Plus ORM, AOP logging
client-module/   JavaFX/FXML desktop client — still a skeleton
```

## Build & Run

```bash
# compile everything (required for multi-module)
mvn compile -DskipTests

# run backend (skip test-compile to avoid easyexcel classpath issue)
mvn compile -DskipTests && mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true
```

**No tests exist** in any module — `mvn test` has nothing to run.

Default admin: `admin / 123456`

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus BaseMapper)
- **Entity → DTO**: manual `*Converter` per domain, no MapStruct
- **Validation groups**: `Create.class` / `Update.class` on request DTOs
- **Soft delete**: MyBatis-Plus `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`.
- **JWT**: access token (2h) + refresh token (7d), custom `JwtAuthInterceptor`. Login: `username` + `password`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`
- **Pagination**: `PageResult<T>` wrapper over MyBatis-Plus `Page`
- **RBAC**: ADMIN / TEACHER / STUDENT, role-based menu routing

## Async Import Pipeline (scores / teachers / students)

- `POST /api/{domain}/batch` (MultipartFile) → `{taskId}`
- `GET /api/scores/{taskId}/progress` — generic polling endpoint
- `GET /api/scores/{taskId}/result` — final result
- Thread pool: `importExecutor` (core=2, max=4, queue=10, `CallerRunsPolicy`)
- Flow: save temp file → create `TaskRecord` → submit Runnable → thread parses Excel → `service.create()` per row → updates progress → complete/fail
- Per-row failure doesn't abort; errors collected in result JSON

### Import task patterns (must follow exactly)

- `@AllArgsConstructor`
- Fields: `taskId`, `fileUrl`, domain service, `taskService`
- Parse: `Path.of(URI.create(fileUrl))` — NOT string replace (breaks on Windows `file:///C:/...`)
- Catch: `catch(Throwable)` — not just Exception
- Progress: `10 + (i+1)*80/size` every 50 rows
- Cleanup: delete temp file in `finally`
- `ObjectMapper` instantiated per invocation (no shared state)
- Use `@Transactional(propagation = REQUIRES_NEW)` on `TaskServiceImpl` methods that run in background threads

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` sets `"createTime"`/`"updateTime"` — auto-fill is dead. Set timestamps manually in service code.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok doesn't copy `@Qualifier` to constructor params. Write a manual constructor (see `TaskController` / `TeacherController` / `StudentController`).
- **`application.yml` in `.gitignore`**: `*.yml` pattern blocks tracking after first commit.
- **`.xlsx` in `.gitignore`**: Excel import template files are not tracked. Put them in `backend-module/` for local testing.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **No AI controllers/services**: AI tables/entities exist but no DeepSeek integration yet.

## Import Templates

Sample .xlsx files at `backend-module/import-teachers-template.xlsx`, `backend-module/import-students-template.xlsx` (not tracked in git).
