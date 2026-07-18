# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2, JavaFX, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups, Excel row models (`dto/report/`)
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

> **CRITICAL**: Adding/editing any class in `common-module` (DTOs, VOs, enums) requires installing the JAR:
> ```bash
> mvn install -DskipTests -pl common-module -am
> ```
> Without this, `spring-boot:run -pl backend-module` may not pick up new fields — no error, fields silently stay null.
> Tests with `-pl backend-module` resolve common-module from `target/classes` — re-compilation suffices.

Default admin: `admin / 123456`
Active profile defaults to `application.yml`; `-Dspring.profiles.active=dev` merges with `application-dev.yml`.

API docs in `API-DOCUMENT.md` (root) and `javafx-frontend/API-DOCUMENT.md`.

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`)
- **Excel**: EasyExcel 4.0.3 for both import (reading) and export (writing). Export row models in `common-module/.../dto/report/` with `@ExcelProperty`
- **Entity → DTO**: manual `*Converter` per domain, no MapStruct. Converters map ID fields only; any identifier-based resolution (studentNo→studentId, teacherNo→teacherId, className→classId, courseName→courseId) is done in the service layer before calling the converter.
- **Identifier lookup mappers**: most use `@Select` with `is_deleted = 0`. For soft-delete recovery, each has `*IncludeDeleted` variants that omit the filter (e.g. `selectByStudentNoIncludeDeleted`, `selectByTeacherNoIncludeDeleted`, `selectByNameIncludeDeleted`, `selectByUsernameIncludeDeleted`).
  Standard lookups: `StudentMapper.selectByStudentNo(String)`, `TeacherMapper.selectByTeacherNo(String)`,
  `ClassMapper.selectByClassNameAndGrade(String, String)`, `ClassMapper.selectByClassName(String)`,
  `CourseMapper.selectByName(String)`
- **Validation groups**: `@Validated(Create.class)` / `@Validated(Update.class)` on request DTOs (not `@Valid`)
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`.
- **JWT**: access token (2h) + refresh token (7d), custom `JwtAuthInterceptor` on `/api/**`. Interceptor sets `request.setAttribute("userId", ...)` and `request.setAttribute("role", ...)`. Login: `username` + `password`. Whitelisted: `/api/auth/login`, `/api/auth/refresh`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`; `AiCallLogAspect` logs AI calls
- **Pagination**: `PageResult.of(records, total, page, size)` from MyBatis-Plus `Page`
- **RBAC**: ADMIN / TEACHER / STUDENT (`RoleEnum`). Controllers use `SecurityHelper.requireAdmin(request)` or `SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN")`. **Do NOT use `@RequireRole`** — it does not exist.
- **All entities** extend `BaseEntity` (id auto, createdAt, updatedAt, isDeleted)

## Controller Authentication Pattern

Every controller that needs role checks follows this pattern (no annotations):
```java
import com.campus.backend.util.SecurityHelper;
import jakarta.servlet.http.HttpServletRequest;

@GetMapping
public ApiResponse<XxxVO> list(@RequestParam(...) ..., HttpServletRequest request) {
    SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
    Long userId = (Long) request.getAttribute("userId");
    // ...
}
```

- `SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN")` — throws 403 if role doesn't match
- `SecurityHelper.requireAdmin(request)` — throws 403 if not ADMIN
- `(Long) request.getAttribute("userId")` — gets the caller's user ID (set by `JwtAuthInterceptor`)

## AI Module

`backend-module/.../ai/` — DeepSeek integration via OkHttp, fully implemented.

| Component | File | Purpose |
|---|---|---|
| `AiService` (interface) | `ai/AiService.java` | `AiResult call(AiRequest)` |
| `DeepSeekProvider` | `ai/DeepSeekProvider.java` | Code-default (`matchIfMissing=true`). yml sets `campus.ai.provider=glm4` currently |
| `GLM4Provider` | `ai/GLM4Provider.java` | Active when `campus.ai.provider=glm4` (current yml default) |
| `LocalMockProvider` | `ai/LocalMockProvider.java` | Opt-in only (`campus.ai.provider=mock`). Never active unless explicitly set |
| `AiServiceFactory` | `ai/AiServiceFactory.java` | Retry with exponential backoff; throws `AI_SERVICE_ERROR` if all attempts fail |
| `AiRateLimiter` | `ai/AiRateLimiter.java` | Redis daily quota (50/day default) |
| `AiCallLogAspect` | `aop/AiCallLogAspect.java` | AOP logs every AI call to `ai_call_log` |
| `AiProperties` | `config/AiProperties.java` | `campus.ai.*` config (API key, model, limits) |

**Prompt templates** live in `common-module/.../constant/PromptTemplate.java` (diagnosis, comment, risk analysis, suggestions).

**AI analysis APIs** (all in `backend-module`):

| Endpoint | Controller | Entity (table) | Notes |
|---|---|---|---|
| `POST/GET /api/diagnoses` | `DiagnosisController` | `AIDiagnosisRecord` (`ai_diagnosis_record`) | Single diagnosis + history |
| `POST /api/comments` | `CommentController` | `AIComment` (`ai_comment`) + `AICommentVersion` | Single comment generation |
| `POST /api/comments/batch` | `CommentController` | Same + `TaskRecord` | Async batch (classId + semester), skips existing |
| `GET/PUT /api/comments` + `/api/comments/{id}` | `CommentController` | Same | List + manual edit (creates new version) |
| `GET /api/risk-warnings` | `RiskWarningController` | `RiskWarning` (`risk_warning`) | List with riskLevel/handleStatus filters |
| `PUT /api/risk-warnings/{id}/handle` | `RiskWarningController` | Same | Mark as RESOLVED |
| `POST /api/risk-warnings/detect` | `RiskWarningController` | Same | Auto-detect risk for all students |
| `GET /api/suggestions` | `SuggestionController` | `AISuggestion` (`ai_suggestion`) | Cached via `diagnosisId`; regenerates if diagnosis changes |

### AI Service best practices (already applied)

- Always check `aiResult.isSuccess()` before using content
- Clean Markdown code fences: `content.replaceAll("^```json\\s*|```$", "").trim()`
- Do NOT put `@Transactional` on methods that call `AiServiceFactory.execute()` (HTTP call)
- Convert `BigDecimal` to `.doubleValue()` before `String.format("%.1f", ...)`

## Testing (pure JUnit + Mockito, no Spring test context)

`@WebMvcTest` / `@SpringBootTest` **will fail** — `@MapperScan` on `CampusApplication` forces DataSource dependency. All tests use standalone MockMvc setup:

```java
MockMvcBuilders.standaloneSetup(controller)
    .setControllerAdvice(new GlobalExceptionHandler())
    .setValidator(new LocalValidatorFactoryBean())
    .build();
```

Test files exist on disk but **are NOT tracked by git** (`.gitignore` has `test/`). Existing tests (in `backend-module/src/test/`): `UserControllerTest`, `SystemControllerTest`, `LogControllerTest`, `AcademicStatsControllerTest`, `GenericTaskControllerTest`, `AcademicStatsServiceImplTest`, `StudentServiceImplTest`, `TaskServiceImplTest`, `AsyncConfigTest`, `WebMvcConfigTest`, `ApiRateLimitInterceptorTest`, `ErrorCodeTest`, `GenerateTestExcel`.

Windows users can run tests via `run_tests.bat` (hardcoded paths).

## Async Import Pipeline

### Domain-specific endpoints
- `POST /api/{domain}/batch` (MultipartFile) → `{taskId}`
- `GET /api/scores/{taskId}/progress` / `.../result` — polling
- `POST /api/comments/batch` — JSON body `{classId, semester}`, no file, async AI

### Generic unified endpoint (`GenericTaskController`)
- `POST /api/tasks?type=SCORE_IMPORT&examId=&courseId=` (MultipartFile) → `{taskId, status}`
- `GET /api/tasks/{taskId}` — progress; `GET /api/tasks/{taskId}/result` — result
- Params passed as request params (not body); `teacherId` injected from request attribute
- `TaskHandler` interface: each handler registers via `getType()` and creates `Runnable` via `createRunnable()`

### Import task patterns (must follow exactly)

- `@AllArgsConstructor`
- Fields: `taskId`, `fileUrl` (only for file-based import), domain service, `taskService`
- Parse: `Path.of(URI.create(fileUrl))` — NOT string replace (breaks on Windows `file:///C:/...`)
- Catch: `catch(Throwable)` — not just Exception
- Progress: `10 + (i+1)*80/size` every 50 rows (or every 10 for AI tasks)
- Cleanup: delete temp file in `finally`
- `ObjectMapper` instantiated per invocation (no shared state)
- `@Transactional(propagation = REQUIRES_NEW)` on `TaskServiceImpl` methods called from background threads

### Thread pool
`importExecutor` (core=2, max=4, queue=10, `CallerRunsPolicy`). Per-row failure doesn't abort; errors collected in result JSON.

## Sync Excel Export

For small-to-medium data, `ReportController` streams `.xlsx` directly to `HttpServletResponse`:

| Endpoint | Params | Notes |
|---|---|---|
| `GET /api/reports/excel/score-table` | `examId`, `courseId`, `classId?` | Score table via `ScoreArchiveVO` |
| `GET /api/reports/excel/comments` | `classId?`, `semester?` | Comments; tries JSON extraction if content is JSON |
| `GET /api/reports/excel/risk-list` | `semester?`, `riskLevel?`, `handleStatus?` | Risk warnings |
| `GET /api/reports/excel/stats` | `examId`, `courseId`, `classId` | Class stats + distribution |

**Pattern**: EasyExcel `write()` on the HTTP response output stream, no temp file, no async. Content-Disposition as `attachment; filename*=UTF-8''{encoded}`.

Additionally, `ClassController` has `GET /api/classes/export/excel` that exports class list via `ClassRow`.

**Export DTOs** in `common-module/.../dto/report/` annotated with `@ExcelProperty`: `ScoreTableRow`, `CommentRow`, `RiskRow`, `ClassRow`.

**Async extension stub**: `ExportTaskHandler` registered as type `EXPORT` in `GenericTaskController` — currently returns "not implemented yet". Accepted via `POST /api/tasks?type=EXPORT&reportType=...`.

## Student-Facing APIs (`/api/my/*`)

All in `MyController`. Uses `SecurityHelper.requireAnyRole(request, "STUDENT")`.

**Key**: JWT's `userId` maps to `Student.userId`, not `Student.id`. Always resolve via `studentMapper.selectByUserId(userId)` first to get the actual `Student.id`.

| Endpoint | Params | Notes |
|---|---|---|
| `GET /api/my/profile` | — | Combines `Student` + `User` + `ClassInfo` into `StudentProfileVO` |
| `GET /api/my/courses` | `semester?` | Queries `TeachingTask` by student's `classId`, then loads `Course` entities |
| `GET /api/my/scores` | `semester?`, `page?`, `size?` | `ScoreMapper.selectByStudentPage()` — paginated, semester filters via JOIN exam |
| `GET /api/my/scores/trend` | — | `ScoreMapper.selectTrend()` — GROUP BY semester, avg/max/min/count |
| `GET /api/my/scores/radar` | `semester?` | `ScoreMapper.selectRadar()` — per-course final scores |
| `GET /api/my/diagnosis` | `page?`, `size?` | Reuses `DiagnosisService.pageHistory(studentId)` |
| `GET /api/my/suggestions` | `semester?` | Reuses `SuggestionService.getSuggestion(studentId, semester, userId)` |
| `GET /api/my/comments` | `semester?`, `page?`, `size?` | `CommentService.listByStudent()` — by studentId + optional semester |
| `GET /api/my/warnings` | `page?`, `size?` | `RiskWarningService.listByStudent()` — by studentId |

## Soft-Delete Recovery (create-with-recovery)

Recovery is embedded in `create()` methods. Pattern: look up by unique key **without** `is_deleted=0` filter, recover if found, insert if not:

```java
ExistingEntity existing = mapper.findByKeyIncludeDeleted(key);
if (existing != null) {
    existing.setIsDeleted(0);
    existing.setUpdatedAt(LocalDateTime.now());
    mapper.recoverByKey(key);
    // Student/Teacher also recover the associated User if soft-deleted
    return existing;
}
// else: create new normally
```

Implemented entities (with unique key → recover method):

| Entity | Key | Mapper recover method |
|--------|-----|-----------------------|
| `ClassInfo` | `className` | `recoverByClassName` |
| `Course` | `name` | `recoverByName` |
| `User` | `username` | `recoverByUsername` |
| `Student` | `studentNo` | `recoverByStudentNo` (+ recovers `User` by `userId`) |
| `Teacher` | `teacherNo` | `recoverByTeacherNo` (+ recovers `User` by `userId`) |

Each `*IncludeDeleted` mapper method uses raw `LIMIT 1` (no `is_deleted` condition). Each `recoverBy*` sets `is_deleted = 0` + `updated_at = NOW()`.

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` fills `"createTime"`/`"updateTime"` — set timestamps manually in service code.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok doesn't copy `@Qualifier`. Write a manual constructor (see `TaskController` / `TeacherController` / `StudentController` / `CommentController`).
- **BusinessException returns HTTP 200**: `GlobalExceptionHandler.handleBusiness()` uses `@ResponseStatus(HttpStatus.OK)` — all business errors (404, 403, 409, etc.) return HTTP 200 with the error code in the JSON body, not the corresponding HTTP status. Don't rely on HTTP status to detect business errors.
- **`.gitignore` traps**: `*.yml` (application config not tracked), `.xlsx` (import templates not tracked), `test/` (test files not committed), `docs/` (documentation not committed), `.log` (not `*.log` — `mvn_test.log` is tracked).
- **application config**: `application.yml` IS tracked (`!application.yml` in `.gitignore` exempts it from `*.yml`). `application-dev.yml`, `application-prod.yml` exist on disk but are gitignored. With `spring.profiles.active=dev` (or `prod`), the dev/prod overrides merge with defaults in `application.yml`.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **JAVA_HOME**: On Linux, `mvn spring-boot:run` fails without it. Use `export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))` (JDK 17).
- **`Map<String, Integer>` for status**: `PUT /{id}/status` endpoints accept `?status=1` query param, not JSON body.
- **`@RequestBody` for batch comments**: `POST /api/comments/batch` uses `@RequestBody Map<String, Object>` (JSON body with classId + semester), NOT `@RequestParam` — follows the no-file async task pattern.
- **Two frontends**: `client-module` (multi-module, less complete) and `javafx-frontend` (standalone, tracked, hybrid FXML + WebView/Vue).
- **`MissingServletRequestParameterException`**: `GlobalExceptionHandler` now catches it and returns 400 `"缺少必填参数: {name}"` (not 500).
- **Comment content may be JSON**: `COMMENT_PROMPT` tells AI to return plain text, but AI sometimes wraps in JSON. Export code in `ReportController` uses `extractContent()` that tries to parse JSON and extract `comment`/`content`/`text` fields as fallback.
- **PostgreSQL `@Select` + `<script>`**: Never use `AND (#{param} IS NULL OR ...)` in a raw `@Select` — PG can't infer the parameter type. Always use `<script>` with `<if test='param != null'>` for nullable conditions.
- **Score lacks `semester`**: `Score` entity stores `examId` but no `semester`. For semester-based filtering, JOIN `score` → `exam` on `exam_id`. Methods like `autoDetect()` in `RiskWarningServiceImpl` query ALL scores without any semester filter.
- **Risk detection rules in `evaluateExpandedRisk()`**: Initial screening checks failCount + absentCount, then promotes level if avgScore < 60 or any course shows consecutive 3-semester decline (S1 > S2 > S3). `buildRiskIndicators()` passes these extra indicators to the AI prompt.
