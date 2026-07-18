package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.async.TeacherImportTask;
import com.campus.backend.entity.Student;
import com.campus.backend.entity.TaskRecord;
import com.campus.backend.entity.Teacher;
import com.campus.backend.mapper.UserMapper;
import com.campus.backend.service.TeacherService;
import com.campus.backend.service.TaskService;
import com.campus.backend.service.UserService;
import com.campus.common.dto.TeacherDTO;
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
@RequestMapping("/api/teachers")
public class TeacherController {
    private final TeacherService teacherService;
    private final TaskService taskService;
    private final ThreadPoolTaskExecutor importExecutor;
    private final UserService userService;

    public TeacherController(TeacherService teacherService, TaskService taskService,
                             @Qualifier("importExecutor") ThreadPoolTaskExecutor importExecutor, UserMapper userMapper, UserService userService) {
        this.teacherService = teacherService;
        this.taskService = taskService;
        this.importExecutor = importExecutor;
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<Teacher> create(@RequestBody TeacherDTO dto) {
        return ApiResponse.success(teacherService.create(dto));
    }

    @PutMapping("/{id}")
    public ApiResponse<Teacher> update(@PathVariable Long id, @RequestBody TeacherDTO dto) {
        dto.setId(id);
        return ApiResponse.success(teacherService.update(dto));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        Teacher teacher = teacherService.findById(id);
        Long userId = teacher.getUserId();
        teacherService.delete(id);
        userService.delete(userId);
        return ApiResponse.success();
    }

    @GetMapping
    public ApiResponse<PageResult<Teacher>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Teacher> result = teacherService.pageList(page, size, keyword);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Student> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        teacherService.toggleStatus(id, status);
        return ApiResponse.success();
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Long>> batchImport(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "teacher_import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(tempDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        TaskRecord task = taskService.createTask("TEACHER_IMPORT", null);

        TeacherImportTask importTask = new TeacherImportTask(
                task.getId(), filePath.toUri().toString(), teacherService, taskService);
        importExecutor.submit(importTask);

        Map<String, Long> result = new HashMap<>();
        result.put("taskId", task.getId());
        return ApiResponse.success(result);
    }

    @GetMapping("/batch")
    public ApiResponse<Void> exportExcel() {
        // EasyExcel 导出逻辑（见 ExcelUtil）
        return ApiResponse.success();
    }
}
