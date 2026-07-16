package com.campus.backend.controller;

import com.campus.backend.entity.TaskRecord;
import com.campus.backend.handler.TaskHandler;
import com.campus.backend.service.TaskService;
import com.campus.common.enums.ErrorCode;
import com.campus.common.vo.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class GenericTaskController {

    private final TaskService taskService;
    private final ThreadPoolTaskExecutor importExecutor;
    private final Map<String, TaskHandler> handlerMap;

    public GenericTaskController(TaskService taskService,
                                 @Qualifier("importExecutor") ThreadPoolTaskExecutor importExecutor,
                                 List<TaskHandler> handlers) {
        this.taskService = taskService;
        this.importExecutor = importExecutor;
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(TaskHandler::getType, h -> h));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> submitTask(
            @RequestParam String type,
            @RequestParam(required = false) MultipartFile file,
            HttpServletRequest request) throws Exception {

        TaskHandler handler = handlerMap.get(type);
        if (handler == null) {
            return ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "未知任务类型: " + type);
        }

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = "import_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(tempDir, fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            fileUrl = filePath.toUri().toString();
        }

        TaskRecord record = taskService.createTask(type, null);

        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        params.put("teacherId", String.valueOf(request.getAttribute("userId")));

        Runnable task = handler.createRunnable(record.getId(), fileUrl, params);
        importExecutor.submit(task);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", record.getId());
        result.put("status", "PENDING");
        return ApiResponse.success(result);
    }

    @GetMapping("/{taskId}")
    public ApiResponse<Map<String, Object>> getProgress(@PathVariable Long taskId) {
        TaskRecord record = taskService.getProgress(taskId);
        if (record == null) {
            return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "任务不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", record.getStatus());
        result.put("progress", record.getProgress());
        result.put("currentCount", record.getCurrentCount());
        result.put("totalCount", record.getTotalCount());
        return ApiResponse.success(result);
    }

    @GetMapping("/{taskId}/result")
    public ApiResponse<Object> getResult(@PathVariable Long taskId) {
        TaskRecord record = taskService.getResult(taskId);
        if (record == null) {
            return ApiResponse.error(ErrorCode.NOT_FOUND.getCode(), "任务不存在");
        }
        if (!"COMPLETED".equals(record.getStatus())) {
            Map<String, Object> info = new HashMap<>();
            info.put("status", record.getStatus());
            info.put("errorMessage", record.getErrorMessage());
            return ApiResponse.success(info);
        }
        try {
            Object parsed = new ObjectMapper().readValue(record.getResultJson(),
                    new TypeReference<Map<String, Object>>() {});
            return ApiResponse.success(parsed);
        } catch (Exception e) {
            return ApiResponse.success(record.getResultJson());
        }
    }
}
