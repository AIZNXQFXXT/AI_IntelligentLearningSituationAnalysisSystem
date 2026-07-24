# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

| Module | Role |
|--------|------|
| `common-module` | Shared DTOs, enums (`ErrorCode`, `RoleEnum`), VOs (`ApiResponse`, `PageResult`), validation groups, prompt templates, Excel row models |
| `backend-module` | Spring Boot REST API on `:8080`, JWT auth, MyBatis-Plus ORM, AOP logging, AI providers (DeepSeek/GLM/Qianfan/Mock via OkHttp), async import pipeline |
| `client-module` | JavaFX/FXML desktop client (OkHttp → backend). 34 FXML views, 35 controllers, 25 models, 17 services. Entry: `com.campus.client.App` |

## Build & Run

```bash
mvn compile -DskipTests                              # compile all
mvn spring-boot:run -pl backend-module               # backend only
mvn javafx:run -pl client-module                     # client only
mvn test -pl backend-module -am -Dtest=FooTest       # single test
mvn install -DskipTests -pl common-module -am        # reinstall common (required after any change!)
```

**Full start**: `bash start.sh` — common install → backend → health poll (2s × 60) → client launch.

After editing a class in `common-module` you **must** `mvn install -DskipTests -pl common-module -am` for the backend to pick up new fields. Tests resolve via `target/classes` — recompilation suffices.

`client-module` hardcodes `<javafx.platform>win</javafx.platform>` — Linux/macOS runs need that overridden, or the javafx-maven-plugin won't find native binaries.

Default admin: `admin / 123456`.

## Gitignore Traps

`*.yml`, `*.txt`, `*.bat`, `.xlsx`, `test/`, `docs/`, `.log`, `logs/`, `.opencode/`, `.env` are all gitignored. Only `!application.yml` (the default, at `backend-module/src/main/resources/application.yml`) is tracked — `-dev.yml`, `-prod.yml`, and any other YAMLs are invisible to git and must be created locally. Credentials pass via `.env` env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_URL`, `REDIS_PORT`, `JWT_SECRET`, `DEEPSEEK_API_KEY`, `GLM_API_KEY`.

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`) — 3-layer plumbing required for new filters
- **Converters**: 13 manual `*Converter` classes (no MapStruct). Map IDs only; identifier resolution in service layer
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`. Unique-key lookup for recovery in `create()` methods
- **Entities** extend `BaseEntity` (`id`, `createdAt`, `updatedAt`, `isDeleted`) — located in `backend-module` only
- **JWT**: access (2h) + refresh (7d). `JwtAuthInterceptor` on `/api/**` sets `request.setAttribute("userId", role)`. Bypasses: `/api/auth/login`, `/api/auth/refresh`, `/api/health`
- **RBAC**: `SecurityHelper.requireAdmin(request)` / `requireAnyRole(request, "TEACHER", "ADMIN")`. Pass `HttpServletRequest` as controller param
- **Business errors**: `BusinessException(ErrorCode)` → `GlobalExceptionHandler` → HTTP 200 + `ApiResponse.error()` with error code. Do not check HTTP status
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok drops `@Qualifier`. Write a manual constructor (see `TaskController`)
- **Pagination**: Backend: `PageResult.of(records, total, page, size)`. Client: same `PageResult` model, JavaFX `Pagination` control
- **GPA formula**: `StatsServiceImpl.bracketGpa()` / `weightedGpa()`. 90-100→4.0-5.0, 80-89→3.0-3.9, etc. `(score - lowerBound) * 0.1 + tierBaseGpa`, weighted = Σ(绩点×学分) ÷ Σ学分

## AI Module

`backend-module/.../ai/` — OkHttp-based provider abstraction. Config key: `campus.ai.provider` (`qianfan` default). All four providers registered unconditionally as `@Service`.

`AiServiceFactory.execute()`: 3-phase retry (primary backoff → connect-timeout poll other real providers → `LocalMockProvider` fallback). Always check `aiResult.isSuccess()`. Strip Markdown fences: `content.replaceAll("^```json\\s*|```$", "").trim()`. No `@Transactional` on methods calling `AiServiceFactory`.

Student JWT resolve: `userId` → `studentMapper.selectByUserId(userId)` (not `Student.id`).

## JavaFX Pagination Pattern (Single Source of Truth)

```java
// initialize(): register ONCE
pagination.setPageFactory(this::buildPage);

// buildPage: guard prevents reload loops
private Label buildPage(int pageIndex) {
    int targetPage = pageIndex + 1;
    if (targetPage != currentPage) {
        currentPage = targetPage;
        loadData();
    }
    return new Label("");
}

// loadData success: ONLY setPageCount — never setCurrentPageIndex / setPageFactory
task.setOnSucceeded(e -> {
    PageResult<T> result = task.getValue();
    tableData.setAll(result.getRecords());
    int pageCount = (int) Math.ceil((double) result.getTotal() / pageSize);
    pagination.setPageCount(Math.max(pageCount, 1));
});

// filter/search reset: 3-step
filterChangeListener = (obs, old, val) -> {
    currentPage = 1;
    pagination.setCurrentPageIndex(0);
    loadData();
};
```

## Testing

19 test files under `backend-module/src/test/`. All use `MockMvcBuilders.standaloneSetup()` + `GlobalExceptionHandler` + `LocalValidatorFactoryBean` — `@WebMvcTest`/`@SpringBootTest` fail due to `@MapperScan` requiring DataSource. `.gitignore` excludes `test/` — force-add with `git add -f` for new test files.

## Known Pitfalls

- **List-page filter plumbing**: Adding a `@RequestParam` filter requires touching **3 layers**: `@GetMapping` → Service interface → Service impl. Spring silently drops undeclared params.
- **Service method changes**: Use `rg` before changing shared methods like `pageHistory()` or `listByStudent()`. All callers (admin + teacher + student controllers) need updating.
- **Converter/VO 6-layer chain**: DB → Entity → VO → Converter → API JSON → client model → FXML column. Empty column? Check converter first.
- **Score lacks `semester`**: Must JOIN `score` → `exam` on `exam_id` for semester filtering.
- **PostgreSQL `<script>` in `@Select`**: Never `AND (#{param} IS NULL OR ...)` — PG can't infer type. Use `<if test='param != null'>`.
- **`MyBatisPlusConfig` auto-fill**: `insertFill()` uses `strictInsertFill` for `createdAt` + `strictUpdateFill` for `updatedAt`. `strictInsertFill` does NOT overwrite non-null values.
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **Top-bar HBox**: `<HBox prefHeight="60" minHeight="60" maxHeight="60">` — all three required to prevent collapse.
- **Comment content may be JSON**: `ReportController.extractContent()` parses JSON and extracts `comment`/`content`/`text` fields.
- **JavaFX `styleClass`**: Must exist in `client-module/src/main/resources/css/styles.css` or it's a silent no-op.
- **Backend easyexcel dependency**: Duplicated in `pom.xml` (two identical declarations). Harmless but a Maven warning.
- **`.gitignore` covers**: `*.yml` (except `application.yml`), `*.txt`, `*.bat`, `.xlsx`, `test/`, `docs/`, `.log`, `.opencode/`, `.env`.

## API Documentation

Maintained at `API-DOCUMENT.md` (441 lines). After adding/changing API endpoints, update it.
