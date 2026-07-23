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
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`. It resolves `username` from `operatorId` via `UserMapper.selectById(...)` and writes both `username` + `operatorId`. When `userId` is null (unauthenticated, or `/api/auth/login` + `/api/auth/refresh` which bypass the JWT interceptor) or the user can't be resolved, `username` falls back to `"游客"` (operatorId stays NULL). Do NOT regress to writing only `operatorId` — the log list UI filters/displays by `username`. `AiCallLogAspect` logs AI calls.
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

- **Auto-fill timestamps**: `BaseEntity` uses `createdAt`/`updatedAt`, and `MyBatisPlusConfig`'s `MetaObjectHandler` fills these via `strictInsertFill`/`strictUpdateFill` on INSERT and INSERT_UPDATE. Time is set automatically — no need to set `setCreatedAt`/`setUpdatedAt` manually in service code. `strictInsertFill` does NOT overwrite a non-null value, so manual timestamps (if any) still win. Legacy service code with manual `setCreatedAt`/`setUpdatedAt` calls is redundant but harmless.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok drops `@Qualifier`. Write a manual constructor (see `TaskController`, `TeacherController`, etc.).
- **BusinessException returns HTTP 200**: `GlobalExceptionHandler.handleBusiness()` uses `@ResponseStatus(HttpStatus.OK)` — all business errors come as HTTP 200 with error code in JSON body. Don't rely on HTTP status.
- **`.gitignore` traps**: `*.yml` excludes most config files (but `!application.yml` exempts the default), `.xlsx`, `test/`, `docs/`, `.log` (not `*.log`), `.opencode/`, `.env`.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **`Map<String, Integer>` for status**: `PUT /{id}/status` accepts `?status=1` query param, not JSON body.
- **Comment content may be JSON**: AI sometimes wraps comment text in JSON. `ReportController.extractContent()` parses JSON and extracts `comment`/`content`/`text` fields as fallback.
- **PostgreSQL `<script>` in `@Select`**: Never use `AND (#{param} IS NULL OR ...)` — PG can't infer type. Use `<if test='param != null'>`.
- **Score lacks `semester`**: JOIN `score` → `exam` on `exam_id` for semester filtering. `RiskWarningServiceImpl.autoDetect()` queries ALL scores.
- **JavaFX `Pagination` reload loop (client-module)**: The 8 list controllers (`OperationLogController`, `AiCallLogController`, `StudentManagementController`, `TeacherManagementController`, `ClassManagementController`, `CourseManagementController`, `ExamManagementController`, `TeachingTaskManagementController`) all use a unified pattern. **Do NOT call `pagination.setCurrentPageIndex(...)` or `pagination.setPageFactory(...)` inside the async `task.setOnSucceeded` callback** — each response would yank the index back to "this response's page", re-triggering the factory → guarded `loadData()` → next response → infinite reload loop + page jitter. Correct pattern: register `pagination.setPageFactory(this::buildPage)` **once** in `initialize()`; in the success callback only update table data + `pagination.setPageCount(...)`. Search/filter reset (`handleSearch` / `onSearchKeyPressed` / filter listeners) sets `currentPage = 1` then calls `pagination.setCurrentPageIndex(0)` before `loadData()` to sync the visual index. New list controllers must follow this exact pattern.
- **List-page query filters need full 3-layer plumbing**: A frontend filter (e.g. `AiCallLogController` success dropdown, `functionName` field) sends `success=1/0` / `functionName=...` as query params, but if the backend `@GetMapping` endpoint doesn't declare the matching `@RequestParam(required = false)`, Spring silently drops the param and the service returns unfiltered data — UI looks "broken but no error". When adding a filter, update **all three layers together**: `LogController.pageAiCalls` (or equivalent) → `LogService` interface signature → `LogServiceImpl` wrapper (`wrapper.eq(...)` / `wrapper.like(...)`). Compare against `/api/logs/operation` which is the reference implementation. Frontend `success` maps to entity `Integer success` (0/1) — type matches.
- **`OperationLogAspect` does NOT write `targetType` / `targetId` / `oldData` / `newData`**: The aspect only fills `operation` (HTTP method), `detail` (method signature), `ip`, `durationMs`, `resultStatus`, `failReason`, `username`/`operatorId`. The four "target" columns are NULL in DB for every row. `targetType` is NOT derivable from the JWT token — token carries operator identity (`userId`/`role`), while `targetType` is the operation's target entity (STUDENT/TEACHER/...) which is orthogonal to identity; same admin token can hit `/api/students` or `/api/teachers`. To populate it later, either parse the `@PostMapping`/`@PutMapping`/`@DeleteMapping` URL path in the aspect (zero-touch, auto-covers all endpoints) or add a `@OperationTarget` annotation on each controller method (precise but touches dozens of methods). The frontend "目标类型" column in `OperationLogView.fxml` has been removed to hide the empty column; restoring it later only needs the FXML + `colTargetType` field/cellValueFactory re-added.
- **Converter/VO must expose every entity field the frontend needs**: DB entity → VO → API JSON → client model → FXML column is a 5-layer chain; a single missing link makes a column silently blank with no error. Example: `AIDiagnosisRecord.aiModel` and `tokensUsed` are written by `DiagnosisServiceImpl` and stored in DB, but `DiagnosisVO` originally omitted both fields and `DiagnosisConverter.toVO()` didn't map them, so the API JSON had no `aiModel` key and the frontend `colAiModel` / `colTokens` columns were always empty. Client model `AiDiagnosis` already had the fields — the break was purely server-side VO/Converter. **Rule**: when a frontend table column is empty but the DB column is populated, check `Entity → VO → Converter` first, not the DB. Adding a field to `common-module` VO requires `mvn install -DskipTests -pl common-module -am` (see Build & Run) before the backend picks it up.
- **JavaFX `styleClass` references a CSS class that must actually exist**: Writing `styleClass="quick-action-btn"` in FXML without a matching `.quick-action-btn { ... }` rule in the loaded `.css` is a silent no-op — JavaFX falls back to the default `.button` style with no warning. Combined with JavaFX `Button`'s default `maxWidth = USE_COMPUTED_SIZE` (which equals preferred width = text width), buttons won't stretch to fill a `VBox`/`HBox` even when `fillWidth=true`. Result: buttons sized purely by text content, so a button with ASCII chars (e.g. "AI诊断" — "AI" is half-width) ends up narrower than all-CJK buttons ("成绩录入"/"批量导入"/"评语管理"). Fix pattern: define the styleClass in `client-module/src/main/resources/css/styles.css` with `-fx-max-width: Infinity` (lets Button exceed `USE_COMPUTED_SIZE` and fill the parent), and use `-fx-alignment: CENTER` + `-fx-text-alignment: CENTER` to center the label. Any new FXML `styleClass=` must be cross-checked against the CSS file.
