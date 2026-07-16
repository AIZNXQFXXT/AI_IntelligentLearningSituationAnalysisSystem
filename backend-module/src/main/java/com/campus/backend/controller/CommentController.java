package com.campus.backend.controller;

import com.campus.backend.service.CommentService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.AICommentDTO;
import com.campus.common.enums.ErrorCode;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.CommentVO;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentVO> generateSingle(
            @Valid @RequestBody AICommentDTO dto,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(commentService.generateSingle(dto, userId));
    }

    @GetMapping
    public ApiResponse<PageResult<CommentVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        return ApiResponse.success(commentService.pageList(page, size, classId, semester));
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