package com.campus.backend.controller;

import com.campus.backend.service.SuggestionService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.SuggestionVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionService suggestionService;

    @GetMapping
    public ApiResponse<SuggestionVO> getSuggestion(
            @RequestParam Long studentId,
            @RequestParam String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(suggestionService.getSuggestion(studentId, semester, userId));
    }
}
