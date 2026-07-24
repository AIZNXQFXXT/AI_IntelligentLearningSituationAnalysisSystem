package com.campus.backend.controller;

import com.campus.backend.ai.AiServiceFactory;
import com.campus.backend.async.CommentBatchTask;
import com.campus.backend.entity.TaskRecord;
import com.campus.backend.mapper.AICommentMapper;
import com.campus.backend.mapper.AICommentVersionMapper;
import com.campus.backend.mapper.ScoreMapper;
import com.campus.backend.mapper.StudentMapper;
import com.campus.backend.service.CommentService;
import com.campus.backend.service.TaskService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.AICommentDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final TaskService taskService;
    private final ThreadPoolTaskExecutor importExecutor;
    private final StudentMapper studentMapper;
    private final ScoreMapper scoreMapper;
    private final AICommentMapper commentMapper;
    private final AICommentVersionMapper versionMapper;
    private final AiServiceFactory aiServiceFactory;

    public CommentController(CommentService commentService, TaskService taskService,
                             @Qualifier("importExecutor") ThreadPoolTaskExecutor importExecutor,
                             StudentMapper studentMapper, ScoreMapper scoreMapper,
                             AICommentMapper commentMapper, AICommentVersionMapper versionMapper,
                             AiServiceFactory aiServiceFactory) {
        this.commentService = commentService;
        this.taskService = taskService;
        this.importExecutor = importExecutor;
        this.studentMapper = studentMapper;
        this.scoreMapper = scoreMapper;
        this.commentMapper = commentMapper;
        this.versionMapper = versionMapper;
        this.aiServiceFactory = aiServiceFactory;
    }

    @PostMapping
    public ApiResponse<CommentVO> generateSingle(
            @Valid @RequestBody AICommentDTO dto,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(commentService.generateSingle(dto, userId));
    }

    @PostMapping("/batch")
    public ApiResponse<Map<String, Object>> batch(
            @RequestBody Map<String, Object> body,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        Long classId = Long.valueOf(body.get("classId").toString());
        String semester = (String) body.get("semester");

        TaskRecord record = taskService.createTask("COMMENT_BATCH", null);

        CommentBatchTask task = new CommentBatchTask(record.getId(), classId, semester, userId,
                commentMapper, versionMapper, studentMapper, scoreMapper, aiServiceFactory, taskService);
        importExecutor.submit(task);

        Map<String, Object> result = new HashMap<>();
        result.put("taskId", record.getId());
        result.put("status", "PENDING");
        return ApiResponse.success(result);
    }

    @GetMapping
    public ApiResponse<PageResult<CommentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        return ApiResponse.success(commentService.pageList(page, size, classId, semester, keyword));
    }

    @PutMapping("/{id}")
    public ApiResponse<CommentVO> update(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        String content = body.get("content");
        if (content == null || content.isBlank()) {
            return ApiResponse.error(ErrorCode.VALIDATION_FAILED);
        }
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(commentService.update(id, content, userId));
    }
}