# Teacher/Student Batch Import Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Implement `POST /api/teachers/batch` and `POST /api/students/batch` Excel import endpoints using the existing async task pipeline.

**Architecture:** Reuse `importExecutor` thread pool + `TaskRecord` polling pattern from `ScoreImportTask`. Each import creates a background `Runnable` that parses Excel with EasyExcel, iterates rows calling the domain `create()` service method (which also creates a `sys_user` login account), and reports progress via `TaskService`.

**Tech Stack:** EasyExcel, Spring thread pool, MyBatis-Plus, JWT auth

## Global Constraints

- Follow exact `ScoreImportTask` pattern: temp file save → TaskRecord creation → background task submission → polling via existing `GET /api/scores/{id}/progress`
- Manual constructor in controllers (no `@AllArgsConstructor`) when using `@Qualifier`
- `@Transactional(propagation = REQUIRES_NEW)` on TaskServiceImpl (already exists)
- Catch `Throwable` in background tasks
- Clean up temp files in `finally`
- `className` in StudentRow is looked up via `ClassMapper` to resolve `classId`

---

### Task 1: Create `TeacherRow` Excel DTO

**File:** `common-module/src/main/java/com/campus/common/dto/TeacherRow.java`

```java
package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TeacherRow {
    @ExcelProperty("工号")
    private String teacherNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("职称")
    private String title;

    @ExcelProperty("学科")
    private String subject;

    @ExcelProperty("学历")
    private String education;

    @ExcelProperty("院系")
    private String department;
}
```

### Task 2: Create `StudentRow` Excel DTO

**File:** `common-module/src/main/java/com/campus/common/dto/StudentRow.java`

```java
package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentRow {
    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("姓名")
    private String name;

    @ExcelProperty("性别")
    private String gender;

    @ExcelProperty("班级")
    private String className;

    @ExcelProperty("入学年份")
    private String enrollYear;

    @ExcelProperty("电话")
    private String phone;

    @ExcelProperty("监护人电话")
    private String guardianPhone;
}
```

### Task 3: Create `TeacherImportTask`

**File:** `backend-module/src/main/java/com/campus/backend/async/TeacherImportTask.java`

Exact copy of `ScoreImportTask` pattern, calling `teacherService.create()` per row.

### Task 4: Create `StudentImportTask`

**File:** `backend-module/src/main/java/com/campus/backend/async/StudentImportTask.java`

Same pattern, with `ClassMapper` injection for `className → classId` lookup in `convertRow()`.

### Task 5: Update `TeacherController` — add `POST /api/teachers/batch`

- Remove `@AllArgsConstructor`, write manual constructor
- Inject `TaskService` + `@Qualifier("importExecutor") ThreadPoolTaskExecutor`
- New endpoint saves temp file, creates `TaskRecord("TEACHER_IMPORT")`, submits `TeacherImportTask`, returns `{taskId}`

### Task 6: Update `StudentController` — add `POST /api/students/batch`

- Same pattern, submits `StudentImportTask` (also injects `ClassMapper` for the background task)
- Task type: `"STUDENT_IMPORT"`

### Final Verification

- [ ] `mvn compile -DskipTests`
