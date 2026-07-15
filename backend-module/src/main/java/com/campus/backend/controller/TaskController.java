package com.campus.backend.controller;

import com.campus.backend.async.ScoreImportTask;
import com.campus.backend.entity.TaskRecord;
import com.campus.backend.service.ScoreService;
import com.campus.backend.service.StudentService;
import com.campus.backend.service.TaskService;
import com.campus.common.vo.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/scores")
public class TaskController {

    private final TaskService taskService;
    private final ScoreService scoreService;
    private final StudentService studentService;
    private final ThreadPoolTaskExecutor importExecutor;

    public TaskController(TaskService taskService, ScoreService scoreService,
                          StudentService studentService,
                          @Qualifier("importExecutor") ThreadPoolTaskExecutor importExecutor) {
        this.taskService = taskService;
        this.scoreService = scoreService;
        this.studentService = studentService;
        this.importExecutor = importExecutor;
    }

    @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
