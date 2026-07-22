# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2). PostgreSQL + Redis. C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, VOs (DashboardVO, ScoreDistributionVO), enums, PageResult, prompt templates, EasyExcel models
backend-module/  Spring Boot REST API :8080, MyBatis-Plus ORM, JWT auth, AI integration (OkHttp), AOP logging
client-module/   JavaFX/FXML desktop client — only frontend module
```

## Build

```bash
mvn compile -DskipTests                                          # compile everything (covers common → backend dep)
mvn compile -pl client-module -am -DskipTests                     # compile client + deps (needed after common-module changes)
mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true     # run backend (JAVA_HOME must be set on Linux)
```

> **After editing `common-module`**: run `mvn install -DskipTests -pl common-module -am` before `spring-boot:run` or backend silently ignores new fields. Tests with `-pl backend-module` resolve from `target/classes` — re-compile only suffices.

## Key Tech & Versions

| Lib | Version |
|-----|---------|
| Spring Boot | 3.2.0 |
| Java | 17 |
| MyBatis-Plus | 3.5.5 |
| JavaFX | 17.0.2 |
| EasyExcel | 4.0.3 |
| JJWT | 0.12.3 |

## Config & Secrets

- Default profile: `application.yml`; dev overrides via `application-dev.yml` (gitignored by `*.yml` rule)
- `.env` loaded via `optional:file:.env[.properties]` — not tracked, contains `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_URL`, `REDIS_PORT`, `JWT_SECRET`, `DEEPSEEK_API_KEY`, `GLM_API_KEY`
- Default admin: `admin / 123456`

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`)
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`. Recovery built into `create()` methods — look up by unique key without `is_deleted` filter, update if found, insert if not.
- **Entity → DTO**: manual `*Converter` per domain (13 converters), no MapStruct. Converters map IDs only; identifier resolution done in service layer.
- **Identifier lookups**: `StudentMapper.selectByStudentNo(String)`, `TeacherMapper.selectByTeacherNo(String)`, `ClassMapper.selectByClassName(String)`, `CourseMapper.selectByName(String)`. Most mappers use `@Select` with `is_deleted = 0`; `*IncludeDeleted` variants omit the filter.
- **JWT**: access token (2h) + refresh token (7d). `JwtAuthInterceptor` on `/api/**` sets `request.setAttribute("userId", ...)` and `request.setAttribute("role", ...)`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`; `AiCallLogAspect` logs AI calls.
- **RBAC**: `SecurityHelper.requireAdmin(request)` / `SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN")`. Pass `HttpServletRequest request` as controller param. Do NOT use `@RequireRole`.
- **Pagination**: `PageResult.of(records, total, page, size)` from MyBatis-Plus `Page`.
- **All entities** extend `BaseEntity` (`Long id`, `LocalDateTime createdAt/updatedAt`, `Integer isDeleted`).
- **Validation**: `@Validated(Create.class)` / `@Validated(Update.class)` on request DTOs, not `@Valid`.
- **Student-facing APIs** at `/api/my/*` (`MyController`). JWT `userId` maps to `Student.userId`, always resolve `studentMapper.selectByUserId(userId)` first.

## AI Module

`backend-module/.../ai/` — AI provider abstraction via OkHttp. Config key: `campus.ai.provider` (`glm4` default, also `deepseek`, `mock`).

`AiServiceFactory.execute()` retries with linear backoff (1s, 2s, 3s), falls back to `LocalMockProvider` if primary fails. Always check `aiResult.isSuccess()` before using content. Clean Markdown fences: `content.replaceAll("^```json\\s*|```$", "").trim()`. Do NOT put `@Transactional` on methods calling `AiServiceFactory` (HTTP call).

Key endpoints: `POST /api/diagnoses`, `POST /api/comments` (single + batch), `POST /api/risk-warnings/detect`, `GET /api/suggestions`. Prompt templates in `common-module/.../constant/PromptTemplate.java`.

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

Test files under `backend-module/src/test/` are mostly gitignored (`test/` pattern). Existing: `UserControllerTest`, `SystemControllerTest`, `LogControllerTest`, `AcademicStatsControllerTest`, `GenericTaskControllerTest`, `AcademicStatsServiceImplTest`, `StudentServiceImplTest`, `SystemServiceImplTest`, `TaskServiceImplTest`, `AsyncConfigTest`, `WebMvcConfigTest`, `ApiRateLimitInterceptorTest`, `ErrorCodeTest`, `GenerateTestExcel`.

```bash
# run a single test
mvn test -pl backend-module -Dtest=StudentServiceImplTest -DfailIfNoTests=false
# run tests with common-module recompilation
mvn test -pl backend-module -am -Dtest=SystemControllerTest,SystemServiceImplTest -DfailIfNoTests=false
```

## Client Module Peculiarities

- IDs in service method signatures are `int`, but common-module DTOs use `Long`. Call `.intValue()` when passing DTO IDs to service methods. Maps keyed by id: handle `Long`→`int` via `.intValue()`.
- `PageResult` type is `com.campus.client.model.PageResult` (client-local), not from common module.
- When doing find-and-replace of model names across the module, check that controller class declarations (`*ManagementController`) are not accidentally renamed.

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` fills `"createTime"`/`"updateTime"` — set timestamps manually in service code.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok drops `@Qualifier`. Write a manual constructor (see `TaskController`, `TeacherController`).
- **BusinessException returns HTTP 200**: `GlobalExceptionHandler.handleBusiness()` uses `@ResponseStatus(HttpStatus.OK)` — all business errors come as HTTP 200 with error code in JSON body.
- **`.gitignore` traps**: `*.yml` excludes most config files (only `!application.yml` exempted), `*.xml` excludes all but `pom.xml`, `test/` excludes test files, `.xlsx`, `docs/`, `.env`, `.opencode/`, `.txt`, `.bat`.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **`Map<String, Integer>` for status**: `PUT /{id}/status` accepts `?status=1` query param, not JSON body.
- **Comment content may be JSON**: AI sometimes wraps text in JSON. `ReportController.extractContent()` parses and extracts `comment`/`content`/`text` fields.
- **PostgreSQL `<script>` in `@Select`**: Never use `AND (#{param} IS NULL OR ...)` — PG can't infer type. Use `<if test='param != null'>`.
- **Score lacks `semester`**: JOIN `score` → `exam` on `exam_id` for semester filtering.
