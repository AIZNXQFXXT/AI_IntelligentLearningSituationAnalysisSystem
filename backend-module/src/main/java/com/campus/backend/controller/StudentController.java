package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.async.StudentImportTask;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.ClassMapper;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.dto.StudentDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService studentService;
    private final TaskService taskService;
    private final ThreadPoolTaskExecutor importExecutor;
    private final ClassMapper classMapper;

    public StudentController(StudentService studentService, TaskService taskService,
                             @Qualifier("importExecutor") ThreadPoolTaskExecutor importExecutor,
                             ClassMapper classMapper) {
        this.studentService = studentService;
        this.taskService = taskService;
        this.importExecutor = importExecutor;
        this.classMapper = classMapper;
    }

    @PostMapping
    public ApiResponse<Student> create(@RequestBody StudentDTO dto) {
        return ApiResponse.success(studentService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Student> update(@PathVariable Long id, @RequestBody StudentDTO dto) {
        dto.setId(id);
        return ApiResponse.success(studentService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Student>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long classId,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        IPage<Student> result = studentService.pageList(page, size, keyword, classId, role, userId);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/my-class")
    public ApiResponse<PageResult<Student>> findMyClassStudents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        Long userId = (Long) request.getAttribute("userId");
        IPage<Student> result = studentService.pageList(page, size, keyword, null, role, userId);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        studentService.toggleStatus(id, status);
        return ApiResponse.success();
    }

    @GetMapping("/export/excel")
    public ApiResponse<Void> exportExcel() {
        return ApiResponse.success();
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Long>> batchImport(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "student_import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(tempDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        TaskRecord task = taskService.createTask("STUDENT_IMPORT", null);

        StudentImportTask importTask = new StudentImportTask(
                task.getId(), filePath.toUri().toString(), studentService, taskService, classMapper);
        importExecutor.submit(importTask);

        Map<String, Long> result = new HashMap<>();
        result.put("taskId", task.getId());
        return ApiResponse.success(result);
    }
}
