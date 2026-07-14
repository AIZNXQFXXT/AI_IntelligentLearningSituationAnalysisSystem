# Async Score Import Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the async Excel score import pipeline — Excel upload, background processing, progress/result query.

**Architecture:** ThreadPoolTaskExecutor + TaskService CRUD + ScoreImportTask with studentNo lookup. Client uploads file, backend saves to temp, creates TaskRecord, submits to executor, returns taskId; client polls progress/result.

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus, EasyExcel 4.0, JUnit 5 (TDD)

## Global Constraints

- No tests exist in any module — first tests in this project
- Follow existing patterns: `@AllArgsConstructor` + final fields (no `@Autowired`), `ApiResponse<T>` returns
- Use constructor injection, not field injection
- Tests go in `backend-module/src/test/java/com/campus/backend/` mirroring source structure

---

### Task 1: Add `findByStudentNo` to StudentService

**Files:**
- Modify: `backend-module/src/main/java/com/campus/backend/service/StudentService.java`
- Modify: `backend-module/src/main/java/com/campus/backend/service/impl/StudentServiceImpl.java`

**Interfaces:**
- Produces: `Student findByStudentNo(String studentNo)` on `StudentService` interface + impl

- [ ] **Step 1: Add method to interface**

Edit `StudentService.java` — add line after `findById`:
```java
Student findByStudentNo(String studentNo);
```

- [ ] **Step 2: Add test for the new method**

Create `backend-module/src/test/java/com/campus/backend/service/impl/StudentServiceImplTest.java`:
```java
package com.campus.backend.service.impl;

import com.campus.backend.entity.Student;
import com.campus.backend.mapper.StudentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentMapper studentMapper;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(studentMapper);
    }

    @Test
    void findByStudentNo_shouldReturnStudent_whenExists() {
        Student expected = new Student();
        expected.setId(1L);
        expected.setStudentNo("2024001");
        expected.setName("张三");

        when(studentMapper.selectOne(any())).thenReturn(expected);

        Student result = studentService.findByStudentNo("2024001");

        assertNotNull(result);
        assertEquals("2024001", result.getStudentNo());
        assertEquals("张三", result.getName());
    }

    @Test
    void findByStudentNo_shouldReturnNull_whenNotExists() {
        when(studentMapper.selectOne(any())).thenReturn(null);

        Student result = studentService.findByStudentNo("9999999");

        assertNull(result);
    }
}
```

- [ ] **Step 3: Run test to verify it fails**

```bash
mvn test -pl backend-module -Dtest="StudentServiceImplTest" -DskipTests=false 2>&1 || true
```
Expected: compilation error — method not implemented

- [ ] **Step 4: Implement the method in StudentServiceImpl**

In `StudentServiceImpl.java`, add:
```java
@Override
public Student findByStudentNo(String studentNo) {
    return studentMapper.selectOne(
        new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo));
}
```

Add imports if missing:
```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.backend.entity.Student;
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn test -pl backend-module -Dtest="StudentServiceImplTest" -DskipTests=false
```
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add backend-module/src/main/java/com/campus/backend/service/StudentService.java backend-module/src/main/java/com/campus/backend/service/impl/StudentServiceImpl.java backend-module/src/test/java/com/campus/backend/service/impl/StudentServiceImplTest.java
git commit -m "feat: add findByStudentNo to StudentService"
```

---

### Task 2: Create ScoreRow DTO

**Files:**
- Create: `common-module/src/main/java/com/campus/common/dto/ScoreRow.java`

**Interfaces:**
- Produces: `com.campus.common.dto.ScoreRow` — EasyExcel row model with Chinese column headers

- [ ] **Step 1: Write test for ScoreRow mapping**

Since ScoreRow is a plain DTO (no behavior), the test verifies the EasyExcel annotations are correct and data binding works.

Create `common-module/src/test/java/com/campus/common/dto/ScoreRowTest.java`:
```java
package com.campus.common.dto;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreRowTest {

    @Test
    void shouldHaveChineseColumnAnnotations() throws Exception {
        // Verify @ExcelProperty annotations exist on all required fields
        java.lang.reflect.Field[] fields = ScoreRow.class.getDeclaredFields();
        assertTrue(fields.length > 0, "ScoreRow should have fields");
        for (java.lang.reflect.Field f : fields) {
            ExcelProperty prop = f.getAnnotation(ExcelProperty.class);
            assertNotNull(prop, "Field " + f.getName() + " missing @ExcelProperty");
            assertNotNull(prop.value(), "Field " + f.getName() + " missing column name");
            assertTrue(prop.value().length > 0, "Field " + f.getName() + " has empty column name");
        }
    }

    @Test
    void shouldReadWriteExcel() {
        ScoreRow row = new ScoreRow();
        row.setStudentNo("2024001");
        row.setRegularScore(new BigDecimal("85.5"));
        row.setExamScore(new BigDecimal("90.0"));
        row.setFinalScore(new BigDecimal("88.0"));
        row.setIsAbsent(0);
        row.setIsCheat(0);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        EasyExcel.write(bos, ScoreRow.class).sheet("test").doWrite(List.of(row));

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        List<ScoreRow> rows = EasyExcel.read(bis).head(ScoreRow.class).sheet().doReadSync();

        assertEquals(1, rows.size());
        assertEquals("2024001", rows.get(0).getStudentNo());
        assertEquals(0, new BigDecimal("88.0").compareTo(rows.get(0).getFinalScore()));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl common-module -Dtest="ScoreRowTest" -DskipTests=false 2>&1 || true
```
Expected: compilation error — ScoreRow class not found

- [ ] **Step 3: Create ScoreRow DTO**

Create `common-module/src/main/java/com/campus/common/dto/ScoreRow.java`:
```java
package com.campus.common.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRow {
    @ExcelProperty("学号")
    private String studentNo;

    @ExcelProperty("平时分")
    private BigDecimal regularScore;

    @ExcelProperty("卷面分")
    private BigDecimal examScore;

    @ExcelProperty("最终分")
    private BigDecimal finalScore;

    @ExcelProperty("缺勤")
    private Integer isAbsent;

    @ExcelProperty("作弊")
    private Integer isCheat;
}
```

- [ ] **Step 4: Add easyexcel dependency to common-module pom.xml**

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>easyexcel</artifactId>
    <version>4.0.3</version>
</dependency>
```

Also add needed test scope dependencies:
```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn test -pl common-module -Dtest="ScoreRowTest" -DskipTests=false
```
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add common-module/pom.xml common-module/src/main/java/com/campus/common/dto/ScoreRow.java common-module/src/test/java/com/campus/common/dto/ScoreRowTest.java
git commit -m "feat: create ScoreRow DTO with EasyExcel Chinese column mapping"
```

---

### Task 3: Create TaskRecordMapper

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/mapper/TaskRecordMapper.java`

**Interfaces:**
- Produces: `TaskRecordMapper` — MyBatis-Plus BaseMapper

- [ ] **Step 1: Create TaskRecordMapper** (no test needed — one-liner `extends BaseMapper`, covered indirectly by TaskServiceImplTest)

Create `backend-module/src/main/java/com/campus/backend/mapper/TaskRecordMapper.java`:
```java
package com.campus.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.backend.entity.TaskRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskRecordMapper extends BaseMapper<TaskRecord> {
}
```

- [ ] **Step 2: Verify compilation**

```bash
mvn compile -pl backend-module -DskipTests
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add backend-module/src/main/java/com/campus/backend/mapper/TaskRecordMapper.java
git commit -m "feat: create TaskRecordMapper"
```

---

### Task 4: Implement TaskServiceImpl

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/service/impl/TaskServiceImpl.java`

**Interfaces:**
- Consumes: `TaskRecordMapper` (from Task 3), `TaskService` interface (already exists)
- Produces: Full `TaskService` implementation

- [ ] **Step 1: Write test for TaskServiceImpl**

Create `backend-module/src/test/java/com/campus/backend/service/impl/TaskServiceImplTest.java`:
```java
package com.campus.backend.service.impl;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.TaskRecordMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRecordMapper taskRecordMapper;

    @Captor
    private ArgumentCaptor<TaskRecord> recordCaptor;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRecordMapper);
    }

    @Test
    void createTask_shouldCreatePendingTask() {
        when(taskRecordMapper.insert(any())).thenAnswer(i -> {
            TaskRecord r = i.getArgument(0);
            r.setId(1L);
            return 1;
        });

        TaskRecord result = taskService.createTask("SCORE_IMPORT", "{\"examId\":1}");

        assertNotNull(result);
        assertEquals("SCORE_IMPORT", result.getTaskType());
        assertEquals("PENDING", result.getStatus());
        assertEquals(Integer.valueOf(0), result.getProgress());
        assertEquals(1L, result.getId());
    }

    @Test
    void getProgress_shouldReturnTaskWithProgress() {
        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setProgress(50);
        expected.setStatus("PROCESSING");
        expected.setCurrentCount(25);
        expected.setTotalCount(50);

        when(taskRecordMapper.selectById(1L)).thenReturn(expected);

        TaskRecord result = taskService.getProgress(1L);

        assertEquals(50, result.getProgress());
        assertEquals("PROCESSING", result.getStatus());
    }

    @Test
    void getResult_shouldReturnTaskWithResult() {
        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setStatus("COMPLETED");
        expected.setResultJson("{\"success\":10}");

        when(taskRecordMapper.selectById(1L)).thenReturn(expected);

        TaskRecord result = taskService.getResult(1L);

        assertEquals("COMPLETED", result.getStatus());
        assertEquals("{\"success\":10}", result.getResultJson());
    }

    @Test
    void updateProgress_shouldUpdateFields() {
        TaskRecord existing = new TaskRecord();
        existing.setId(1L);
        existing.setProgress(0);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        taskService.updateProgress(1L, 50, 25, 50);

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals(Integer.valueOf(50), updated.getProgress());
        assertEquals(Integer.valueOf(25), updated.getCurrentCount());
        assertEquals(Integer.valueOf(50), updated.getTotalCount());
    }

    @Test
    void complete_shouldSetCompletedStatus() {
        TaskRecord existing = new TaskRecord();
        existing.setId(1L);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        taskService.complete(1L, "{\"success\":10}");

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals("COMPLETED", updated.getStatus());
        assertEquals("{\"success\":10}", updated.getResultJson());
    }

    @Test
    void fail_shouldSetFailedStatus() {
        TaskRecord existing = new TaskRecord();
        existing.setId(1L);

        when(taskRecordMapper.selectById(1L)).thenReturn(existing);

        taskService.fail(1L, "File format error");

        verify(taskRecordMapper).updateById(recordCaptor.capture());
        TaskRecord updated = recordCaptor.getValue();
        assertEquals("FAILED", updated.getStatus());
        assertEquals("File format error", updated.getErrorMessage());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl backend-module -Dtest="TaskServiceImplTest" -DskipTests=false 2>&1 || true
```
Expected: compilation error — TaskServiceImpl not found

- [ ] **Step 3: Implement TaskServiceImpl**

Create `backend-module/src/main/java/com/campus/backend/service/impl/TaskServiceImpl.java`:
```java
package com.campus.backend.service.impl;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.TaskRecordMapper;
import com.campus.backend.service.TaskService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRecordMapper taskRecordMapper;

    @Override
    public TaskRecord createTask(String taskType, String params) {
        TaskRecord record = new TaskRecord();
        record.setTaskType(taskType);
        record.setStatus("PENDING");
        record.setProgress(0);
        record.setCurrentCount(0);
        record.setTotalCount(0);
        taskRecordMapper.insert(record);
        return record;
    }

    @Override
    public TaskRecord getProgress(Long taskId) {
        return taskRecordMapper.selectById(taskId);
    }

    @Override
    public TaskRecord getResult(Long taskId) {
        return taskRecordMapper.selectById(taskId);
    }

    @Override
    public void updateProgress(Long taskId, int progress, int current, int total) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setProgress(progress);
            record.setCurrentCount(current);
            record.setTotalCount(total);
            taskRecordMapper.updateById(record);
        }
    }

    @Override
    public void complete(Long taskId, String resultJson) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setStatus("COMPLETED");
            record.setResultJson(resultJson);
            record.setProgress(100);
            taskRecordMapper.updateById(record);
        }
    }

    @Override
    public void fail(Long taskId, String error) {
        TaskRecord record = taskRecordMapper.selectById(taskId);
        if (record != null) {
            record.setStatus("FAILED");
            record.setErrorMessage(error);
            taskRecordMapper.updateById(record);
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl backend-module -Dtest="TaskServiceImplTest" -DskipTests=false
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend-module/src/main/java/com/campus/backend/service/impl/TaskServiceImpl.java backend-module/src/test/java/com/campus/backend/service/impl/TaskServiceImplTest.java
git commit -m "feat: implement TaskServiceImpl"
```

---

### Task 5: Create AsyncConfig

**Files:**
- Create: `backend-module/src/main/java/com/campus/backend/config/AsyncConfig.java`

- [ ] **Step 1: Write test for AsyncConfig** (direct instantiation, no Spring context)

Create `backend-module/src/test/java/com/campus/backend/config/AsyncConfigTest.java`:
```java
package com.campus.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void importExecutor_shouldHaveCorrectConfig() {
        AsyncConfig config = new AsyncConfig();
        ThreadPoolTaskExecutor executor = config.importExecutor();

        assertNotNull(executor);
        assertEquals(2, executor.getCorePoolSize());
        assertEquals(4, executor.getMaxPoolSize());
        assertTrue(executor.getThreadNamePrefix().startsWith("import-"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl backend-module -Dtest="AsyncConfigTest" -DskipTests=false 2>&1 || true
```
Expected: compilation error — AsyncConfig not found

- [ ] **Step 3: Create AsyncConfig**

Create `backend-module/src/main/java/com/campus/backend/config/AsyncConfig.java`:
```java
package com.campus.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {

    @Bean(name = "importExecutor")
    public ThreadPoolTaskExecutor importExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("import-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl backend-module -Dtest="AsyncConfigTest" -DskipTests=false
```
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add backend-module/src/main/java/com/campus/backend/config/AsyncConfig.java backend-module/src/test/java/com/campus/backend/config/AsyncConfigTest.java
git commit -m "feat: add ThreadPoolTaskExecutor config for async import"
```

---

### Task 6: Implement ScoreImportTask convertRow() and TaskController

**Files:**
- Modify: `backend-module/src/main/java/com/campus/backend/async/ScoreImportTask.java`
- Modify: `backend-module/src/main/java/com/campus/backend/controller/TaskController.java`

**Interfaces:**
- Consumes: `scoreService`, `taskService`, `studentService`, `importExecutor`
- Produces: Complete async import flow

- [ ] **Step 1: Write test for ScoreImportTask**

Create `backend-module/src/test/java/com/campus/backend/async/ScoreImportTaskTest.java`:
```java
package com.campus.backend.async;

import com.campus.backend.entity.Score;
import com.campus.backend.entity.Student;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.dto.ScoreDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreImportTaskTest {

    @Mock
    private ScoreService scoreService;
    @Mock
    private TaskService taskService;
    @Mock
    private StudentService studentService;

    @Test
    void convertRow_shouldMapCorrectly() throws Exception {
        // Use reflection to call private convertRow method
        ScoreImportTask task = new ScoreImportTask(
            1L, "file:///test.xlsx", 1L, 10L, 20L,
            scoreService, taskService, studentService
        );

        Student student = new Student();
        student.setId(100L);
        student.setStudentNo("2024001");
        when(studentService.findByStudentNo("2024001")).thenReturn(student);

        ScoreRow row = new ScoreRow();
        row.setStudentNo("2024001");
        row.setRegularScore(new BigDecimal("85.0"));
        row.setExamScore(new BigDecimal("90.0"));
        row.setFinalScore(new BigDecimal("88.0"));
        row.setIsAbsent(0);
        row.setIsCheat(0);

        java.lang.reflect.Method method = ScoreImportTask.class.getDeclaredMethod("convertRow", ScoreRow.class);
        method.setAccessible(true);
        ScoreDTO dto = (ScoreDTO) method.invoke(task, row);

        assertNotNull(dto);
        assertEquals(100L, dto.getStudentId());
        assertEquals(10L, dto.getExamId());
        assertEquals(20L, dto.getCourseId());
        assertEquals(0, new BigDecimal("85.0").compareTo(dto.getRegularScore()));
        assertEquals(0, new BigDecimal("90.0").compareTo(dto.getExamScore()));
        assertEquals(0, new BigDecimal("88.0").compareTo(dto.getFinalScore()));
        assertEquals(Integer.valueOf(0), dto.getIsAbsent());
        assertEquals(Integer.valueOf(0), dto.getIsCheat());
    }

    @Test
    void convertRow_shouldThrowWhenStudentNotFound() throws Exception {
        ScoreImportTask task = new ScoreImportTask(
            1L, "file:///test.xlsx", 1L, 10L, 20L,
            scoreService, taskService, studentService
        );

        when(studentService.findByStudentNo("9999999")).thenReturn(null);

        ScoreRow row = new ScoreRow();
        row.setStudentNo("9999999");
        row.setFinalScore(new BigDecimal("88.0"));

        java.lang.reflect.Method method = ScoreImportTask.class.getDeclaredMethod("convertRow", ScoreRow.class);
        method.setAccessible(true);

        assertThrows(RuntimeException.class, () -> {
            try {
                method.invoke(task, row);
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl backend-module -Dtest="ScoreImportTaskTest" -DskipTests=false 2>&1 || true
```
Expected: compilation error — ScoreRow uses different package or method not found

- [ ] **Step 3: Modify ScoreImportTask**

Replace `ScoreImportTask.java` content:
```java
package com.campus.backend.async;

import com.alibaba.excel.EasyExcel;
import com.campus.common.dto.ScoreRow;
import com.campus.backend.entity.Student;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.dto.ScoreDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class ScoreImportTask implements Runnable {
    private final Long taskId;
    private final String fileUrl;
    private final Long teacherId;
    private final Long examId;
    private final Long courseId;
    private final ScoreService scoreService;
    private final TaskService taskService;
    private final StudentService studentService;

    @Override
    public void run() {
        Path tempFile = null;
        try {
            // 1. 处理 fileUrl — 可能为 file: 协议路径
            String path = fileUrl.replace("file://", "").replace("file:", "");
            tempFile = Paths.get(path);

            // 2. 解析 Excel
            List<ScoreRow> rows;
            try (InputStream is = Files.newInputStream(tempFile)) {
                rows = EasyExcel.read(is).head(ScoreRow.class).sheet().doReadSync();
            }

            if (rows == null || rows.isEmpty()) {
                taskService.complete(taskId, "{\"success\":0,\"failed\":0,\"errors\":[]}");
                return;
            }

            taskService.updateProgress(taskId, 10, 0, rows.size());

            // 3. 逐行处理
            int success = 0, failed = 0;
            List<Map<String, Object>> errors = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                try {
                    ScoreDTO dto = convertRow(rows.get(i));
                    scoreService.create(dto, teacherId);
                    success++;
                } catch (Exception e) {
                    failed++;
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", i + 2);
                    error.put("reason", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                    errors.add(error);
                }
                if (i % 50 == 0 || i == rows.size() - 1) {
                    int progress = 10 + (int) ((long) (i + 1) * 80 / rows.size());
                    taskService.updateProgress(taskId, progress, success + failed, rows.size());
                }
            }

            // 4. 完成
            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("failed", failed);
            result.put("errors", errors);
            taskService.complete(taskId, new ObjectMapper().writeValueAsString(result));

        } catch (Exception e) {
            taskService.fail(taskId, e.getMessage() != null ? e.getMessage() : "未知错误");
        } finally {
            // 5. 清理临时文件
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); } catch (Exception ignored) {}
            }
        }
    }

    private ScoreDTO convertRow(ScoreRow row) {
        Student student = studentService.findByStudentNo(row.getStudentNo());
        if (student == null) {
            throw new RuntimeException("学号 " + row.getStudentNo() + " 不存在");
        }
        ScoreDTO dto = new ScoreDTO();
        dto.setStudentId(student.getId());
        dto.setExamId(this.examId);
        dto.setCourseId(this.courseId);
        dto.setRegularScore(row.getRegularScore());
        dto.setExamScore(row.getExamScore());
        dto.setFinalScore(row.getFinalScore());
        dto.setIsAbsent(row.getIsAbsent() != null ? row.getIsAbsent() : 0);
        dto.setIsCheat(row.getIsCheat() != null ? row.getIsCheat() : 0);
        return dto;
    }
}
```

- [ ] **Step 4: Write test for TaskController**

Create `backend-module/src/test/java/com/campus/backend/controller/TaskControllerTest.java`:
```java
package com.campus.backend.controller;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.service.TaskService;
import com.campus.common.vo.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;
    @Mock
    private ThreadPoolTaskExecutor importExecutor;

    @Test
    void getProgress_shouldReturnTaskProgress() {
        TaskController controller = new TaskController(taskService, importExecutor);

        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setProgress(50);
        expected.setStatus("PROCESSING");
        when(taskService.getProgress(1L)).thenReturn(expected);

        ApiResponse<TaskRecord> response = controller.getProgress(1L);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(50, response.getData().getProgress());
    }

    @Test
    void getResult_shouldReturnTaskResult() {
        TaskController controller = new TaskController(taskService, importExecutor);

        TaskRecord expected = new TaskRecord();
        expected.setId(1L);
        expected.setStatus("COMPLETED");
        expected.setResultJson("{\"success\":10,\"failed\":0}");
        when(taskService.getResult(1L)).thenReturn(expected);

        ApiResponse<TaskRecord> response = controller.getResult(1L);

        assertEquals(200, response.getCode());
        assertEquals("COMPLETED", response.getData().getStatus());
        assertNotNull(response.getData().getResultJson());
    }
}
```

- [ ] **Step 5: Run tests to verify they fail**

```bash
mvn test -pl backend-module -Dtest="ScoreImportTaskTest,TaskControllerTest" -DskipTests=false 2>&1 || true
```
Expected: compilation errors — implementation not present

- [ ] **Step 6: Create TaskController**

Replace `TaskController.java`:
```java
package com.campus.backend.controller;

import com.campus.backend.async.ScoreImportTask;
import com.campus.backend.entity.TaskRecord;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ScoreService scoreService;
    private final StudentService studentService;
    @Qualifier("importExecutor")
    private final ThreadPoolTaskExecutor importExecutor;
    private final HttpServletRequest request;

    @PostMapping(value = "/import-scores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Long>> importScores(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId,
            @RequestParam("courseId") Long courseId) throws Exception {

        Long teacherId = (Long) request.getAttribute("userId");

        // 1. 保存文件到临时目录
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(tempDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 2. 创建任务记录
        TaskRecord task = taskService.createTask("SCORE_IMPORT", null);

        // 3. 提交异步任务
        ScoreImportTask importTask = new ScoreImportTask(
                task.getId(), filePath.toUri().toString(), teacherId,
                examId, courseId,
                scoreService, taskService, studentService
        );
        importExecutor.submit(importTask);

        Map<String, Long> result = new HashMap<>();
        result.put("taskId", task.getId());
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}/progress")
    public ApiResponse<TaskRecord> getProgress(@PathVariable Long id) {
        return ApiResponse.success(taskService.getProgress(id));
    }

    @GetMapping("/{id}/result")
    public ApiResponse<TaskRecord> getResult(@PathVariable Long id) {
        return ApiResponse.success(taskService.getResult(id));
    }
}
```

- [ ] **Step 7: Run tests to verify they pass**

```bash
mvn test -pl backend-module -Dtest="ScoreImportTaskTest,TaskControllerTest" -DskipTests=false
```
Expected: BUILD SUCCESS

- [ ] **Step 8: Full compilation check**

```bash
mvn clean compile -DskipTests
```
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add backend-module/src/main/java/com/campus/backend/async/ScoreImportTask.java backend-module/src/main/java/com/campus/backend/controller/TaskController.java backend-module/src/test/java/com/campus/backend/async/ScoreImportTaskTest.java backend-module/src/test/java/com/campus/backend/controller/TaskControllerTest.java
git commit -m "feat: implement ScoreImportTask convertRow and TaskController endpoints"
```

---

### Task 7: Verify integration — compile and run all tests

- [ ] **Step 1: Run all tests**

```bash
mvn clean test -DskipTests=false 2>&1
```
Expected: BUILD SUCCESS, all tests pass

- [ ] **Step 2: Compile without tests as final check**

```bash
mvn clean compile -DskipTests
```
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit any remaining changes**

```bash
git add -A
git commit -m "chore: finalize async score import implementation"
```
