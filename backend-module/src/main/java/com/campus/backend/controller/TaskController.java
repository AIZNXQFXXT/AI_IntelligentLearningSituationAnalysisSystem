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

    @PostMapping(value = "/import-scores", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Long>> importScores(
            @RequestParam("file") MultipartFile file,
            @RequestParam("examId") Long examId,
            @RequestParam("courseId") Long courseId,
            HttpServletRequest request) throws Exception {

        Long teacherId = (Long) request.getAttribute("userId");

        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = "import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(tempDir, fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        TaskRecord task = taskService.createTask("SCORE_IMPORT", null);

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
