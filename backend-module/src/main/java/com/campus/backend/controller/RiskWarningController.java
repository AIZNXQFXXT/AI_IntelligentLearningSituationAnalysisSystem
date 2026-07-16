package com.campus.backend.controller;

import com.campus.backend.service.RiskWarningService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import com.campus.common.vo.RiskWarningVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/risk-warnings")
@RequiredArgsConstructor
public class RiskWarningController {

    private final RiskWarningService riskWarningService;

    @GetMapping
    public ApiResponse<PageResult<RiskWarningVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String riskLevel,
            @RequestParam(required = false) String handleStatus,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        return ApiResponse.success(riskWarningService.pageList(page, size, riskLevel, handleStatus));
    }

    @PostMapping("/detect")
    public ApiResponse<Void> detect(
            @RequestParam String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        riskWarningService.autoDetect(semester, userId);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/handle")
    public ApiResponse<Void> handle(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        riskWarningService.handle(id, body.get("remark"), userId);
        return ApiResponse.success();
    }
}