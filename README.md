# AI 智能校园学情分析系统（AI Campus）

基于 **JavaFX 桌面客户端 + Spring Boot 后端** 的 C/S 架构学业分析平台。面向**管理员 / 教师 / 学生**三种角色，覆盖基础数据管理、成绩管理、学情统计与 AI 智能学情诊断（诊断报告、评语生成、学习建议、风险预警）。

> 项目背景与完整需求见 [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md)。

## 功能特性

### 基础数据管理（管理员）
- 学生 / 教师 / 课程 / 班级 / 考试批次 / 教学任务（教师-班级-课程绑定）的增删改查
- 学生、教师 Excel 批量导入（异步任务 + 进度轮询）与导出
- 班主任绑定、选修改（ELECTIVE / REQUIRED / MAJOR）等课程类型管理

### 成绩管理（教师 / 管理员）
- 成绩录入、提交审核、归档；修改全程留痕（成绩修改审计）
- Excel 批量导入：异步执行，前端实时展示导入进度与结果
- 缺考、违纪标记，平时分 / 考试分 / 总评分分开管理

### 学情统计
- 班级统计、年级分析、学科分析、排名（班级 / 年级）、成绩趋势与分数段分布
- GPA 计算：分段绩点（90-100 → 4.0-5.0、80-89 → 3.0-3.9 …），加权 GPA = Σ(绩点×学分) ÷ Σ学分

### AI 智能
- **学情诊断**： strengths / weaknesses / 趋势分析 / 风险等级
- **评语生成**： AI 生成 + 版本管理，支持教师改写
- **学习建议**： 短期 / 长期目标、每日计划、学习资源推荐
- **风险预警**： 风险等级评定、原因分析、处理闭环（处理人 + 处理备注）

### 系统管理（管理员）
- 操作日志、AI 调用日志（模型、Token 用量、耗时、成本）审计
- 动态系统配置、数据字典、异步任务状态查询

### 角色 / 认证
- 三种角色：`ADMIN` / `TEACHER` / `STUDENT`，接口级 RBAC 校验
- JWT 双 Token：access 2 小时 + refresh 7 天，自动续期

## 技术栈

| 层次 | 技术 |
|------|------|
| 语言 | Java 17 |
| 后端 | Spring Boot 3.2、Spring AOP、spring-security-crypto（BCrypt） |
| ORM | MyBatis-Plus 3.5.5（逻辑删除、字段自动填充） |
| 数据库 / 缓存 | PostgreSQL、Redis（Lettuce） |
| 认证 | JJWT 0.12.3（access + refresh 双 Token） |
| AI 接入 | OkHttp 4.12（DeepSeek / GLM / Qianfan / 本地 Mock 四种 Provider） |
| Excel | EasyExcel 4.0.3（后端解析导入）、Apache POI 5.2.5（客户端导出） |
| 客户端 | JavaFX 17（FXML + CSS）、Jackson 2.16 |

## 系统架构

Maven 多模块工程：

```
┌─────────────────┐   HTTP(OkHttp + JWT)   ┌──────────────────────────┐
│  client-module   │ ─────────────────────► │      backend-module       │
│  JavaFX 桌面端    │ ◄───────────────────── │  Spring Boot REST :8080   │
│  35 个 FXML 视图  │    JSON(ApiResponse)   │  controller→service→mapper│
└─────────────────┘                        └───────────┬──────────────┘
        ▲                                              │
        │ 共享 DTO/VO/枚举/Excel 行模型                    │ MyBatis-Plus / Lettuce
        └────────────── common-module ─────────────────┘
                                                    ┌────┴────┐
                                                    │PostgreSQL│  Redis
                                                    └─────────┘
```

- **common-module**：前后端共享的 DTO、VO、枚举（`ErrorCode`、`RoleEnum`）、统一响应（`ApiResponse`、`PageResult`）、校验分组、AI Prompt 模板、Excel 行模型。
- **backend-module**：REST API（23 个 Controller、21 个 Service、21 个 Mapper）、JWT 认证拦截器、AOP 操作日志、异步导入任务、AI Provider 抽象（三段式重试：主 Provider 退避重试 → 连接超时轮询其他 Provider → 最终降级 `LocalMockProvider`）。
- **client-module**：JavaFX 桌面端，35 个 FXML 视图、35 个控制器、17 个服务，按角色展示不同工作台。

## 环境要求

| 依赖 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | 前后端共用 |
| Maven | 3.6+ | |
| PostgreSQL | 14+ | 数据库名 `ai_campus` |
| Redis | 7+ | 未启动时应用仍可运行（懒连接），限流等功能降级 |
| 操作系统 | Windows | 客户端硬编码 `javafx.platform=win` |

## 快速开始

### 1. 初始化数据库

创建 `ai_campus` 数据库后，执行初始化脚本：

```bash
psql -U postgres -d ai_campus -f backend-module/src/main/resources/db/init.sql
```

### 2. 配置 `.env`

在项目根目录创建 `.env`（`application.yml` 通过 `spring.config.import` 自动加载）：

| 环境变量 | 必填 | 说明 |
|----------|:----:|------|
| `DB_URL` | ✅ | PostgreSQL 地址，如 `localhost:5432` |
| `DB_USERNAME` | ✅ | 数据库用户名 |
| `DB_PASSWORD` | ✅ | 数据库密码 |
| `REDIS_URL` | ✅ | Redis 地址，如 `localhost` |
| `REDIS_PORT` | ✅ | Redis 端口，如 `6379` |
| `REDIS_PASSWORD` | — | Redis 密码（无密码则留空） |
| `JWT_SECRET` | ✅ | JWT 签名密钥 |
| `DEEPSEEK_API_KEY` | — | DeepSeek，AI 功能可选 |
| `GLM_API_KEY` | — | 智谱 GLM，AI 功能可选 |
| `QIANFAN_API_KEY` | — | Qianfan / OpenAI 兼容接口，AI 功能可选 |

### 3. 启动

**一键启动（Windows）**：

```bat
start.bat
```

脚本自动完成：安装 common-module → 后台启动后端（日志写入 `backend.log`）→ 轮询 `http://localhost:8080/api/health` 直至就绪 → 启动 JavaFX 客户端；退出时自动清理 8080 端口进程。

**手动启动**：

```bash
# 1. 安装共享模块（common-module 有改动后必须重新执行）
mvn install -DskipTests -pl common-module -am

# 2. 启动后端（:8080）
mvn spring-boot:run -pl backend-module

# 3. 另开终端，启动客户端
mvn javafx:run -pl client-module
```

### 4. 登录

默认管理员账号：**`admin` / `123456`**

## AI 能力配置

后端通过 `campus.ai.provider` 切换 AI 服务，四种 Provider 均已注册：

| Provider | 说明 |
|----------|------|
| `qianfan` | 默认值，兼容 OpenAI 接口协议 |
| `deepseek` | DeepSeek（`deepseek-chat`） |
| `glm4` | 智谱 GLM |
| `localmock` | 本地 Mock，无需任何 API Key |

每个 Provider 独立配置 `api-key` / `base-url` / `model`，另有每日调用限额（`daily-limit`）、连接 / 读取超时与最大重试次数。

**无 API Key 也能完整运行**：`AiServiceFactory` 在主 Provider 调用失败（含连接超时）时会依次尝试其他真实 Provider，最终降级到 `LocalMockProvider` 返回模拟结果，AI 相关功能不会阻塞系统。

## 项目结构

```
ai-campus-system
├── common-module/                 # 共享 DTO/VO/枚举/校验/Prompt 模板/Excel 行模型
├── backend-module/                # Spring Boot REST API (:8080)
│   └── src/main/
│       ├── java/com/campus/backend/
│       │   ├── controller/        # 23 个 Controller
│       │   ├── service/impl/      # 业务实现（统计、GPA、AI 编排）
│       │   ├── mapper/            # MyBatis-Plus BaseMapper
│       │   ├── entity/            # 20 个业务实体 + BaseEntity
│       │   ├── ai/                # AI Provider 抽象与工厂
│       │   ├── async/             # 异步导入/批处理任务
│       │   ├── security/          # JWT、限流拦截器
│       │   └── config/            # 全局异常、MyBatis-Plus、异步配置
│       └── resources/
│           ├── application.yml
│           └── db/init.sql        # 数据库初始化脚本
├── client-module/                 # JavaFX 桌面客户端
│   └── src/main/resources/
│       ├── fxml/                  # 35 个视图
│       └── css/styles.css
├── docs/
│   └── REQUIREMENTS.md            # 需求规格说明书（v1.0）
├── API-DOCUMENT.md                # API 文档：21 组 / 约 99 个端点
├── AGENTS.md                      # 开发规范与架构约定
└── start.bat                      # Windows 一键启动脚本
```

## API 文档

完整接口说明（请求方法、路径、参数、权限）见 [API-DOCUMENT.md](API-DOCUMENT.md)，共 21 组、约 99 个端点，覆盖认证、用户、学生、教师、课程、班级、考试、成绩、教学任务、统计、工作台、AI 诊断 / 评语 / 建议、风险预警、Excel 报表、异步任务、日志与系统配置。

统一约定：

- 所有响应为 HTTP 200 + `ApiResponse` 包装，业务错误通过错误码区分（`GlobalExceptionHandler`）
- 分页返回 `PageResult`（records / total / page / size）
- JWT 通过请求头携带，登录 / 刷新 / 健康检查接口豁免

## 测试

```bash
# 全部后端测试
mvn test -pl backend-module -am

# 单个测试类
mvn test -pl backend-module -am -Dtest=StatsControllerTest
```

测试基于 `MockMvcBuilders.standaloneSetup()` + `GlobalExceptionHandler` + `LocalValidatorFactoryBean`，不依赖真实数据库。

## 常见问题

<details>
<summary><b>Redis 未启动会影响运行吗？</b></summary>

不会。Redis 客户端为懒连接，后端可正常启动；仅限流、缓存等相关功能在调用时降级。
</details>

<details>
<summary><b>为什么改了 common-module 后端不生效？</b></summary>

backend / client 依赖本地仓库中的 common-module 构件，改动后必须重新执行：

```bash
mvn install -DskipTests -pl common-module -am
```
</details>

<details>
<summary><b>Linux / macOS 能运行客户端吗？</b></summary>

client-module 硬编码了 `javafx.platform=win`，非 Windows 环境需覆盖该属性：

```bash
mvn javafx:run -pl client-module -Djavafx.platform=linux
```
</details>

<details>
<summary><b>8080 端口被占用？</b></summary>

`start.bat` 退出时会自动清理 8080 端口进程；手动启动时可修改 `application.yml` 中的 `server.port`。
</details>

## 相关文档

- [docs/REQUIREMENTS.md](docs/REQUIREMENTS.md) — 需求规格说明书（功能需求 F1–F33、优先级矩阵、版本路线）
- [API-DOCUMENT.md](API-DOCUMENT.md) — REST API 文档
- [AGENTS.md](AGENTS.md) — 开发规范：分层约定、已知陷阱、构建命令
