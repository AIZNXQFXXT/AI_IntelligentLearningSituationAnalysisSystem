# 校园教务管理系统 — API 接口文档

> 基础路径：`http://localhost:8080`
>
> 认证方式：`Authorization: Bearer {accessToken}`（除登录/刷新外所有接口都需要）

---

## 通用响应格式

### 成功响应
```json
{"code":200,"msg":"success","data":{...}}
```

### 错误响应
```json
{"code":400,"msg":"错误描述","data":null}
```

### 分页响应
```json
{"code":200,"msg":"success","data":{"records":[...],"total":100,"page":1,"size":20,"pages":5}}
```
> 分页查询：`?page=1&size=20`（page 默认 1，size 默认 20）

### 实体通用字段（所有实体继承 BaseEntity）
```
id: Long, createdAt, updatedAt, isDeleted: Integer(0=正常,1=删除)
```

---

## 1. 认证 Auth — `/api/auth`

### POST `/api/auth/login` — 登录（无需认证）
```json
// Request
{"username":"admin","password":"123456"}
// Response data
{"accessToken":"eyJ...","refreshToken":"eyJ...","role":"ADMIN","userId":1}
```

### POST `/api/auth/refresh` — 刷新 Token（无需认证）
```json
// Request
{"refreshToken":"eyJ..."}
// Response data: 同登录
```

### POST `/api/auth/logout` — 登出
Headers: `Authorization: Bearer {accessToken}`

### PATCH `/api/auth/profile` — 修改个人信息
```json
{"phone":"13800138000","avatar":"http://..."}
```

### PUT `/api/auth/password` — 修改密码
```json
{"oldPassword":"123456","newPassword":"654321"}
```

---

## 2. 用户管理 — `/api/users`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/users` | 列表分页，?role=ADMIN/TEACHER/STUDENT |
| POST | `/api/users` | 创建 |
| PUT | `/api/users/{id}/status` | 启用/禁用 `?status=1` |
| PUT | `/api/users/{id}/password` | 重置密码 |

**User 实体：** id, username, role, avatar, phone, status

**创建 Body：**
```json
{"username":"teacher01","password":"123456","role":"TEACHER"}
```

---

## 3. 学生管理 — `/api/students`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/students` | 列表分页，?keyword, classId |
| GET | `/api/students/my-class` | 当前教师的班级学生 |
| POST | `/api/students` | 创建 |
| PUT | `/api/students/{id}` | 修改 |
| DELETE | `/api/students/{id}` | 删除（软删除） |
| PATCH | `/api/students/{id}/status` | `?status=1` |
| POST | `/api/students/batch` | Excel 批量导入(multipart) → `{"taskId":1}` |
| GET | `/api/students/export/excel` | 导出（暂为 stub） |

**Student 实体：** id, userId, studentNo, name, gender, classId, enrollYear, status, phone, guardianPhone

**创建 Body：**
```json
{"studentNo":"2024001","name":"张三","gender":"男","classId":1,"enrollYear":"2024","phone":"13800138000","guardianPhone":"13900139000","username":"zs","password":"123456"}
```
> 标识符二选一：classId 或 className+grade

**修改 Body：** 同创建，字段均为可选

---

## 4. 教师管理 — `/api/teachers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teachers` | 列表分页，?keyword |
| POST | `/api/teachers` | 创建 |
| PUT | `/api/teachers/{id}` | 修改 |
| DELETE | `/api/teachers/{id}` | 删除 |
| PATCH | `/api/teachers/{id}/status` | `?status=1` |
| POST | `/api/teachers/batch` | Excel 批量导入 → `{"taskId":1}` |
| GET | `/api/teachers/batch` | 导出（stub） |

**Teacher 实体：** id, userId, teacherNo, name, title, subject, education, department, status

**创建 Body：**
```json
{"teacherNo":"T001","name":"李老师","title":"副教授","subject":"计算机","education":"硕士","department":"信息学院","username":"t01","password":"123456"}
```

**修改 Body：** 同创建，字段均为可选

---

## 5. 课程管理 — `/api/courses`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/courses` | 列表分页，?keyword |
| GET | `/api/courses/list` | 全部列表（无分页） |
| POST | `/api/courses` | 创建 |
| PUT | `/api/courses/{id}` | 修改 |
| DELETE | `/api/courses/{id}` | 删除 |
| PATCH | `/api/courses/{id}/status` | `?status=1` |
| GET | `/api/courses/export/excel` | 导出（stub） |

**Course 实体：** id, name, type, credit, description, status

**创建 Body：**
```json
{"name":"高等数学","type":"必修","credit":4.0,"description":"高等数学上册"}
```

**修改 Body：** 同创建，字段均为可选

---

## 6. 班级管理 — `/api/classes`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/classes` | 列表分页，?keyword |
| GET | `/api/classes/my` | 当前教师的班级 |
| GET | `/api/classes/list` | 全部列表（无分页） |
| POST | `/api/classes` | 创建 |
| PUT | `/api/classes/{id}` | 修改 |
| DELETE | `/api/classes/{id}` | 删除 |
| GET | `/api/classes/export/excel` | 导出 Excel（直接下载 .xlsx） |

**ClassInfo 实体：** id, grade, className, headTeacherId, studentCount

**创建 Body：**
```json
{"grade":"2024","className":"计算机一班","headTeacherId":1}
```
> 标识符二选一：headTeacherId 或 teacherNo

**修改 Body：** 同创建，字段均为可选

---

## 7. 考试管理 — `/api/exams`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/exams` | 列表分页，?keyword |
| POST | `/api/exams` | 创建 |
| PUT | `/api/exams/{id}` | 修改 |
| DELETE | `/api/exams/{id}` | 删除 |
| PATCH | `/api/exams/{id}/archive` | 归档/取消归档 |
| GET | `/api/exams/export/excel` | 导出（stub） |

**Exam 实体：** id, name, type（MOCK/MIDTERM/FINAL/RETEST）, semester, classId, examDate, isArchived

**创建 Body：**
```json
{"name":"期中考试","type":"MIDTERM","semester":"2024-2025-1","classId":1,"examDate":"2024-11-15"}
```
> 标识符二选一：classId 或 className+grade

**修改 Body：** 同创建，字段均为可选

---

## 8. 成绩管理 — `/api/scores`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/scores` | 列表分页，?examId, courseId, classId, minScore, maxScore |
| GET | `/api/scores/archive/overview` | 归档概览分页 |
| POST | `/api/scores` | 创建 |
| PUT | `/api/scores/{id}` | 修改 |
| PUT | `/api/scores/{id}/status` | 审核状态 `{"status":1}` (0=DRAFT,1=ARCHIVED,2=SUBMITTED) |
| POST | `/api/scores/batch` | Excel 批量导入(multipart), ?examId, courseId → `{"taskId":1}` |

**Score 实体：** id, studentId, examId, courseId, regularScore, examScore, finalScore, rankClass, rankGrade, isAbsent, isCheat, auditStatus, enteredBy, reason

**创建 Body：**
```json
{"studentId":1,"examId":1,"courseId":1,"regularScore":30.0,"examScore":65.0,"finalScore":85.5,"isAbsent":0,"isCheat":0}
```
> 标识符二选一：studentId 或 studentNo，courseId 或 courseName

**修改 Body：**
```json
{"finalScore":90.0,"reason":"期末成绩更新"}
```
> 修改时 `reason` 必传，其余字段均为可选

**ScoreArchiveVO（归档概览返回）：** id, studentId, studentName, studentNo, examId, examName, courseId, courseName, classId, className, regularScore, examScore, finalScore, rankClass, rankGrade, isAbsent, isCheat, auditStatus, semester, createdAt, updatedAt

---

## 9. 教学任务 — `/api/teaching-tasks`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/teaching-tasks` | 列表分页，?keyword |
| GET | `/api/teaching-tasks/{id}` | 按 ID 查询 |
| GET | `/api/teaching-tasks/teacher/{teacherId}` | 按教师 |
| GET | `/api/teaching-tasks/class/{classId}` | 按班级 |
| POST | `/api/teaching-tasks` | 创建 |
| PUT | `/api/teaching-tasks/{id}` | 修改 |
| DELETE | `/api/teaching-tasks/{id}` | 删除 |

**TeachingTask 实体：** id, teacherId, classId, courseId, semester

**创建 Body：**
```json
{"teacherId":1,"classId":1,"courseId":1,"semester":"2024-2025-1"}
```
> 标识符二选一：teacherId 或 teacherNo，classId 或 className，courseId 或 courseName

**修改 Body：** 同创建，字段均为可选

---

## 10. 统计分析 — `/api/stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/class/{classId}` | 班级统计，?examId, courseId → ClassStatsVO |
| GET | `/api/stats/score-distribution` | 分数段分布，?examId, courseId, classId |
| GET | `/api/stats/ranking` | 排名，?examId, courseId, classId |
| GET | `/api/stats/trend` | 趋势，?classId, courseId |
| GET | `/api/stats/grade-points` | 学生绩点（GPA），?classId（必填）, courseId（可选）→ GradePointVO[] |
| GET | `/api/stats/course-grades` | 课程成绩统计（平均/最高/最低），?classId（必填）→ CourseGradeVO[] |

**ClassStatsVO：** classId, className, courseName, totalStudents, scoredStudents, avgScore, passRate, maxScore, minScore, medianScore, excellentCount, goodCount, mediumCount, passCount, failCount

**ScoreDistributionVO：** `[{"rangeLabel":"90-100","count":10,"percentage":20.0}]`

**RankingItemVO：** `[{"rank":1,"studentId":1,"studentNo":"2024001","studentName":"张三","classId":1,"className":"计算机一班","finalScore":98.0}]`

**TrendItemVO：** `[{"semester":"2024-2025-1","studentCount":48,"avgScore":78.5,"passRate":85.0,"maxScore":98.0,"minScore":35.0}]`

**GradePointVO：** `[{"studentId":1,"studentNo":"2024001","studentName":"张三","gpa":4.25,"courseCount":6}]`
- GPA 5 档（90~100→4.0~5.0, 80~89→3.0~3.9, 70~79→2.0~2.9, 60~69→1.0~1.9, <60→0）
- 传 `courseId` 时返回单科绩点（取该学生最近一条成绩），不传时返回该班学生全部课程的学分加权平均绩点

**CourseGradeVO：** `[{"courseName":"高等数学","avgScore":78.5,"maxScore":98.0,"minScore":45.0,"studentCount":48}]`

---

## 11. 学业统计 — `/api/academic-stats`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/academic-stats/grade-summary` | 年级汇总，?grade, courseId → GradeSummaryVO[] |
| GET | `/api/academic-stats/course-summary` | 课程汇总，?grade, courseId → CourseSummaryVO[] |
| GET | `/api/academic-stats/risk-distribution` | 风险分布，?grade, groupBy → RiskDistributionVO[] |

**GradeSummaryVO/CourseSummaryVO：** grade/courseName, totalStudents, scoredStudents, avgScore, passRate, failCount, excellentCount, goodCount, mediumCount, passCount

**RiskDistributionVO：** dimension, dimensionValue, highRiskCount, mediumRiskCount, lowRiskCount, totalCount

---

## 12. 看板概览 — `/api/stats/*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/stats/overview` | 管理看板 → DashboardVO (classCount, teacherCount, studentCount, courseCount) |
| GET | `/api/stats/school-overview` | 学校概览 → SchoolOverviewVO (totalStudents, totalClasses, totalCourses, totalExams, avgScore, passRate, failRate, excellentCount...) |
| GET | `/api/stats/risk-summary` | 风险摘要 → RiskSummaryVO (totalWarnings, highRiskCount, mediumRiskCount, lowRiskCount, handledCount, unhandledCount) |

---

## 13. 学生端 My — `/api/my`（仅 STUDENT）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/my/profile` | 个人信息 → StudentProfileVO |
| GET | `/api/my/courses` | 我的课程，?semester → Course[] |
| GET | `/api/my/scores` | 我的成绩分页，?semester |
| GET | `/api/my/scores/trend` | 成绩趋势 → ScoreTrendVO[] |
| GET | `/api/my/scores/radar` | 雷达图，?semester → ScoreRadarVO[] |
| GET | `/api/my/diagnosis` | 诊断记录分页 |
| GET | `/api/my/suggestions` | 学习建议，?semester → SuggestionVO |
| GET | `/api/my/comments` | 评语分页，?semester |
| GET | `/api/my/warnings` | 风险预警分页 |

**StudentProfileVO：** studentNo, name, gender, className, enrollYear, avatar, phone, guardianPhone

**ScoreTrendVO：** semester, avgScore, maxScore, minScore, count

**ScoreRadarVO：** courseName, finalScore

---

## 14. AI 诊断 — `/api/diagnoses`

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| POST | `/api/diagnoses` | TEACHER/ADMIN | 生成诊断 `{"studentId":1,"semester":"2024-2025-1"}` → DiagnosisVO |
| GET | `/api/diagnoses` | 通用 | 诊断记录分页，?studentId |

**DiagnosisVO：** id, studentId, studentName, className, semester, overall, strengths[{subject, reason}], weaknesses[{subject, reason}], trend, suggestions[], riskLevel, diagnosisText, createdAt

---

## 15. AI 评语 — `/api/comments`

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| POST | `/api/comments` | TEACHER/ADMIN | 生成单条 `{"studentId":1,"semester":"2024-2025-1"}` → CommentVO |
| POST | `/api/comments/batch` | TEACHER/ADMIN | 批量生成 `{"classId":1,"semester":"..."}` → `{"taskId":1,"status":"PENDING"}` |
| GET | `/api/comments` | TEACHER/ADMIN | 评语列表分页，?classId, semester |
| PUT | `/api/comments/{id}` | TEACHER/ADMIN | 编辑 `{"content":"..."}` → CommentVO（新版本） |

**CommentVO：** id, studentId, studentName, studentNo, className, semester, content, isTeacherEdited, status, generatedBy, createdAt, updatedAt

---

## 16. 风险预警 — `/api/risk-warnings`

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/risk-warnings` | TEACHER/ADMIN | 列表分页，?riskLevel, handleStatus |
| POST | `/api/risk-warnings/detect` | TEACHER/ADMIN | 自动检测 `?semester=...` |
| PUT | `/api/risk-warnings/{id}/handle` | TEACHER/ADMIN | 处理 `{"remark":"..."}` |

**RiskWarningVO：** id, studentId, studentName, studentNo, className, semester, riskLevel, riskReason, aiAnalysis, handleStatus, handlerName, handleRemark, handleAt, createdAt, recommendations[]

---

## 17. AI 建议 — `/api/suggestions`

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| GET | `/api/suggestions` | TEACHER/ADMIN | ?studentId, semester → SuggestionVO |

**SuggestionVO：** id, studentId, studentName, className, semester, shortTerm[], longTerm[], dailyPlan, resources[{subject, items[]}], createdAt

---

## 18. 报表导出 — `/api/reports/excel`（直接下载 .xlsx）

| 方法 | 路径 | 必填参数 | 可选参数 |
|------|------|----------|----------|
| GET | `/api/reports/excel/score-table` | examId, courseId | classId |
| GET | `/api/reports/excel/comments` | — | classId, semester |
| GET | `/api/reports/excel/risk-list` | — | semester, riskLevel, handleStatus |
| GET | `/api/reports/excel/stats` | examId, courseId, classId | — |

> 响应类型：`application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`

---

## 19. 异步任务 — `/api/tasks`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/tasks` | 提交任务(multipart)，?type=SCORE_IMPORT/STUDENT_IMPORT/TEACHER_IMPORT/EXPORT/COMMENT_BATCH, file → `{"taskId":1,"status":"PENDING"}` |
| GET | `/api/tasks/{taskId}` | 查询进度 → `{"status":"PROCESSING","progress":50,"currentCount":25,"totalCount":50}` |
| GET | `/api/tasks/{taskId}/result` | 查询结果 |

---

## 20. 日志管理 — `/api/logs`（仅 ADMIN）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/logs/operation` | 操作日志分页，?username, operation, targetType, resultStatus, startDate, endDate |
| GET | `/api/logs/ai-calls` | AI 调用日志分页 |

**OperationLog 字段：** id, username, operatorId, operation, targetType, targetId, detail, oldData, newData, ip, userAgent, durationMs, resultStatus, failReason

**AICallLog 字段：** id, callerId, callerRole, functionName, aiModel, requestBody, responseBody, httpStatus, tokensInput, tokensOutput, tokensTotal, estimatedCost, durationMs, success, errorMessage, promptTemplate

---

## 21. 系统管理 — `/api/system`（仅 ADMIN）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/system/configs` | 配置列表，?key |
| PUT | `/api/system/configs` | 批量保存 |
| GET | `/api/system/dicts` | 字典列表，?typeCode, status |
| POST | `/api/system/dicts` | 创建字典项 |
| PUT | `/api/system/dicts/{id}` | 修改字典项 |

**SysConfig：** id, configKey, configValue, description

**批量保存 Body：**
```json
[{"configKey":"SCORE_PASS_RATE","configValue":"60","description":"及格线"}]
```

**创建字典项 Body：**
```json
{"typeCode":"SCORE_LEVEL","itemCode":"A","itemValue":"优秀","sortOrder":1}
```

**修改字典项 Body：**
```json
{"typeCode":"SCORE_LEVEL","itemCode":"A","itemValue":"优秀(90-100)","sortOrder":1,"status":1}
```

**SysDict：** id, typeCode, itemCode, itemValue, sortOrder, status

---

**接口总数：93 | 文档版本 1.0**
