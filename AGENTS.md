# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups, prompt templates, Excel row models
backend-module/  Spring Boot REST API :8080, JWT auth, MyBatis-Plus ORM, AOP logging, AI integration, controllers
client-module/   JavaFX/FXML desktop client (older/incomplete, part of root pom.xml)
```

`client-module` is the only frontend module — there is no standalone `javafx-frontend/`.

## Build & Run

```bash
# compile everything
mvn compile -DskipTests

# run backend (JAVA_HOME must be set on Linux)
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true

# run a single test class
mvn test -pl backend-module -Dtest=StudentServiceImplTest -DfailIfNoTests=false

# run tests with common-module recompilation
mvn test -pl backend-module -am -Dtest=SystemControllerTest,SystemServiceImplTest -DfailIfNoTests=false
```

> **CRITICAL**: Adding/editing any class in `common-module` requires installing the JAR:
> ```bash
> mvn install -DskipTests -pl common-module -am
> ```
> Otherwise `spring-boot:run -pl backend-module` silently ignores new fields.
> Tests with `-pl backend-module` resolve `common-module` from `target/classes` — re-compilation suffices.

Default admin: `admin / 123456`. Active profile defaults to `application.yml`; `-Dspring.profiles.active=dev` merges with `application-dev.yml`.

Credentials and secrets come from `.env` (not tracked): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_URL`, `REDIS_PORT`, `JWT_SECRET`, `DEEPSEEK_API_KEY`, `GLM_API_KEY`.

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`)
- **Entity → DTO**: manual `*Converter` per domain (13 converters), no MapStruct. Converters map IDs only; identifier resolution (studentNo→studentId, etc.) done in service layer.
- **Identifier lookups**: most mappers use `@Select` with `is_deleted = 0`. Each has `*IncludeDeleted` variants omitting that filter for soft-delete recovery.
  Key methods: `StudentMapper.selectByStudentNo(String)`, `TeacherMapper.selectByTeacherNo(String)`, `ClassMapper.selectByClassName(String)`, `CourseMapper.selectByName(String)`
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`.
- **JWT**: access token (2h) + refresh token (7d). `JwtAuthInterceptor` on `/api/**` sets `request.setAttribute("userId", ...)` and `request.setAttribute("role", ...)`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`; `AiCallLogAspect` logs AI calls.
- **RBAC**: `SecurityHelper.requireAdmin(request)` / `SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN")`. Pass `HttpServletRequest request` as controller param. Do NOT use `@RequireRole`.
- **Pagination**: `PageResult.of(records, total, page, size)` from MyBatis-Plus `Page`.
- **All entities** extend `BaseEntity` (`Long id`, `LocalDateTime createdAt/updatedAt`, `Integer isDeleted`).
- **Validation**: `@Validated(Create.class)` / `@Validated(Update.class)` on request DTOs, not `@Valid`.

## AI Module

`backend-module/.../ai/` — AI provider abstraction via OkHttp. Config key: `campus.ai.provider` (`glm4` default, also `deepseek` or `mock`).

`AiServiceFactory.execute()` retries with linear backoff (1s, 2s, 3s), falls back to `LocalMockProvider` if primary fails. Always check `aiResult.isSuccess()` before using content. Clean Markdown fences: `content.replaceAll("^```json\\s*|```$", "").trim()`. Do NOT put `@Transactional` on methods calling `AiServiceFactory` (HTTP call).

AI analysis endpoints: `POST /api/diagnoses`, `POST /api/comments` (single + batch), `POST /api/risk-warnings/detect`, `GET /api/suggestions`. Prompt templates in `common-module/.../constant/PromptTemplate.java`.

Student-facing APIs at `/api/my/*` (`MyController`). Key: JWT `userId` maps to `Student.userId`, not `Student.id`. Always resolve `studentMapper.selectByUserId(userId)` first.

## Async Import Pipeline

- Domain-specific: `POST /api/{domain}/batch` (MultipartFile) → `{taskId}`. Poll `GET /api/scores/{taskId}/progress` / `result`.
- Generic: `POST /api/tasks?type=SCORE_IMPORT&examId=&courseId=` (MultipartFile). `GET /api/tasks/{taskId}` for progress.
- Batch comments: `POST /api/comments/batch` with `@RequestBody Map<String, Object>` (JSON `{classId, semester}`), NOT `@RequestParam`.
- File path parsing: `Path.of(URI.create(fileUrl))` — NOT string replace (breaks on Windows `file:///C:/...`).
- Catch `Throwable`, progress every 50 rows (or every 10 for AI), delete temp file in `finally`.
- `ObjectMapper` per invocation (no shared state). `@Transactional(propagation = REQUIRES_NEW)` on `TaskServiceImpl` methods called from background threads.
- Thread pool: `importExecutor` (core=2, max=4, queue=10, `CallerRunsPolicy`).

## Sync Excel Export

`ReportController` streams `.xlsx` via EasyExcel: `GET /api/reports/excel/score-table`, `/comments`, `/risk-list`, `/stats`. No temp file, no async. Content-Disposition: `attachment; filename*=UTF-8''{encoded}`. Export DTOs in `common-module/.../dto/report/` with `@ExcelProperty`.

## Testing

`@WebMvcTest` / `@SpringBootTest` **will fail** (`@MapperScan` forces DataSource). All tests use `MockMvcBuilders.standaloneSetup()` with `GlobalExceptionHandler` and `LocalValidatorFactoryBean`.

Test files under `backend-module/src/test/` — most are gitignored by `test/` in `.gitignore`, but 3 files (`AsyncConfigTest`, `StudentServiceImplTest`, `TaskServiceImplTest`) were tracked before the pattern was added. New tests won't be tracked unless force-added.

Existing test classes (on disk): `UserControllerTest`, `SystemControllerTest`, `LogControllerTest`, `AcademicStatsControllerTest`, `GenericTaskControllerTest`, `AcademicStatsServiceImplTest`, `StudentServiceImplTest`, `SystemServiceImplTest`, `TaskServiceImplTest`, `AsyncConfigTest`, `WebMvcConfigTest`, `ApiRateLimitInterceptorTest`, `ErrorCodeTest`, `GenerateTestExcel`.

## Soft-Delete Recovery

Embedded in `create()` methods: look up by unique key **without** `is_deleted` filter, recover if found, insert if not. Entities with recovery: `ClassInfo` (`className`), `Course` (`name`), `User` (`username`), `Student` (`studentNo`, also recovers `User`), `Teacher` (`teacherNo`, also recovers `User`).

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` fills `"createTime"`/`"updateTime"` — set timestamps manually in service code. The meta-object handler names don't match the field names.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok drops `@Qualifier`. Write a manual constructor (see `TaskController`, `TeacherController`, etc.).
- **BusinessException returns HTTP 200**: `GlobalExceptionHandler.handleBusiness()` uses `@ResponseStatus(HttpStatus.OK)` — all business errors come as HTTP 200 with error code in JSON body. Don't rely on HTTP status.
- **`.gitignore` traps**: `*.yml` excludes most config files (but `!application.yml` exempts the default), `.xlsx`, `test/`, `docs/`, `.log` (not `*.log`), `.opencode/`, `.env`.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **`Map<String, Integer>` for status**: `PUT /{id}/status` accepts `?status=1` query param, not JSON body.
- **Comment content may be JSON**: AI sometimes wraps comment text in JSON. `ReportController.extractContent()` parses JSON and extracts `comment`/`content`/`text` fields as fallback.
- **PostgreSQL `<script>` in `@Select`**: Never use `AND (#{param} IS NULL OR ...)` — PG can't infer type. Use `<if test='param != null'>`.
- **Score lacks `semester`**: JOIN `score` → `exam` on `exam_id` for semester filtering. `RiskWarningServiceImpl.autoDetect()` queries ALL scores.
