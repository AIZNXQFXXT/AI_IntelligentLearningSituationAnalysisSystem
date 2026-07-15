# AI Campus — Agent Guide

Maven multi-module (Java 17, Spring Boot 3.2, JavaFX, MyBatis-Plus, PostgreSQL, Redis). C/S desktop app for academic analysis.

## Modules

```
common-module/   DTOs, enums (ErrorCode, RoleEnum), VOs (ApiResponse, PageResult), validation groups
backend-module/  Spring Boot REST API :8080, JWT auth, MyBatis-Plus ORM, AOP logging
client-module/   JavaFX/FXML desktop client
```

## Build & Run

```bash
# compile everything (required for multi-module)
mvn compile -DskipTests

# run backend (JAVA_HOME must be set on Linux)
export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
mvn compile -DskipTests && mvn spring-boot:run -pl backend-module -Dmaven.test.skip=true

# single test class
mvn test -pl backend-module -Dtest=UserControllerTest -DfailIfNoTests=false
```

Default admin: `admin / 123456`

## Architecture

- **Layers**: controller → service(interface) → impl → mapper(MyBatis-Plus `BaseMapper`)
- **Entity → DTO**: manual `*Converter` per domain, no MapStruct
- **Validation groups**: `@Validated(Create.class)` / `@Validated(Update.class)` on request DTOs (not `@Valid`)
- **Soft delete**: `is_deleted` (0=active, 1=deleted). Never `DELETE FROM`.
- **JWT**: access token (2h) + refresh token (7d), custom `JwtAuthInterceptor` on `/api/**`. Login: `username` + `password`.
- **AOP**: `OperationLogAspect` logs `@PostMapping`/`@PutMapping`/`@DeleteMapping`
- **Pagination**: `PageResult.of(records, total, page, size)` from MyBatis-Plus `Page`
- **RBAC**: ADMIN / TEACHER / STUDENT, role-based menu routing, `SecurityHelper.requireAdmin(request)` for admin-only endpoints
- **All entities** extend `BaseEntity` (id auto, createdAt, updatedAt, isDeleted)

## Testing (pure JUnit + Mockito, no Spring test context)

`@WebMvcTest` / `@SpringBootTest` **will fail** — `@MapperScan` on `CampusApplication` forces DataSource dependency. All tests use standalone setup:

```java
// Controller (MockMvc standalone)
MockMvcBuilders.standaloneSetup(controller)
    .setControllerAdvice(new GlobalExceptionHandler())
    .setValidator(new LocalValidatorFactoryBean())
    .build();

// Service
@ExtendWith(MockitoExtension.class)
class XxxServiceTest {
    @Mock private XxxMapper mapper;
}
```

Existing tests: `UserControllerTest`, `SystemControllerTest`, `LogControllerTest`, `StudentServiceImplTest`, `SystemServiceImplTest`, `TaskServiceImplTest`, `AsyncConfigTest`.

## Async Import Pipeline (scores / teachers / students)

- `POST /api/{domain}/batch` (MultipartFile) → `{taskId}`
- `GET /api/scores/{taskId}/progress` / `.../result` — generic polling
- Thread pool: `importExecutor` (core=2, max=4, queue=10, `CallerRunsPolicy`)
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

## Auth Endpoints

```
POST /api/auth/login     → {token, refreshToken, role, username, userId}
POST /api/auth/logout    → Redis blacklists current token until expiry
POST /api/auth/refresh   → body: {refreshToken} → {token, refreshToken}
PATCH /api/auth/profile  → body: {avatar?, phone?} (partial)
PUT  /api/auth/password  → body: {oldPassword, newPassword}
```

- `/api/auth/login` and `/api/auth/refresh` are whitelisted in `JwtAuthInterceptor`
- Logout stores token in Redis key `blacklist:{token}` with TTL matching remaining validity
- Interceptor checks Redis blacklist before accepting any token

## Stats Endpoints

```
GET /api/stats/class/{classId}?examId=&courseId=            → ClassStatsVO
GET /api/stats/score-distribution?examId=&courseId=&classId= → List<ScoreDistributionVO>
GET /api/stats/ranking?examId=&courseId=&classId=            → List<RankingItemVO>
GET /api/stats/trend?classId=&courseId=                      → List<TrendItemVO>
```

StatsController uses `LambdaQueryWrapper` + Java computation — no raw SQL.

## Log Endpoints

```
GET /api/logs/operation?page=&size=&username=&operation=&targetType=&resultStatus=&startDate=&endDate=
→ 200 { code, data: { records, total, page, size } }
→ 403 (non ADMIN)
```

## Known Pitfalls

- **Broken auto-fill**: `BaseEntity` uses `createdAt`/`updatedAt` but `MyBatisPlusConfig` fills `"createTime"`/`"updateTime"` — auto-fill is dead. Set timestamps manually in service code.
- **`@AllArgsConstructor` + `@Qualifier`**: Lombok doesn't copy `@Qualifier` to constructor params. Write a manual constructor (see `TaskController` / `TeacherController` / `StudentController`).
- **`.gitignore` traps**: `*.yml` (application config not tracked after first commit), `.xlsx` (import templates not tracked).
- **Redis unreachable**: App starts fine (Lettuce lazy connect).
- **No AI controllers/services**: `ai/` package exists but empty — no DeepSeek integration yet.
- **`Map<String, Integer>` for status**: `PUT /{id}/status` endpoints accept `{"status": 1}` and convert via `convertStatus()` (1→ARCHIVED, 2→SUBMITTED, default→DRAFT).
- **JAVA_HOME**: On Linux, `mvn spring-boot:run` may fail without JAVA_HOME set. Use `export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))` (JDK 17).
