package com.campus.backend.controller;

import com.campus.backend.service.DiagnosisService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.dto.AIDiagnosisDTO;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.DiagnosisVO;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnoses")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    @PostMapping
    public ApiResponse<DiagnosisVO> diagnose(
            @Valid @RequestBody AIDiagnosisDTO dto,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN");
        Long userId = (Long) request.getAttribute("userId");
        return ApiResponse.success(diagnosisService.diagnose(dto, userId));
    }

    @GetMapping
    public ApiResponse<PageResult<DiagnosisVO>> history(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String semester,
            HttpServletRequest request) {
        SecurityHelper.requireAnyRole(request, "TEACHER", "ADMIN", "STUDENT");
        return ApiResponse.success(diagnosisService.pageHistory(page, size, studentId, semester));
    }

    /**
     * 一次性回填：把已有诊断记录的 riskLevel 同步到 risk_warning 表。
     * 仅用于历史数据迁移，新数据在生成诊断时已自动同步。
     */
    @PostMapping("/backfill-risk-warnings")
    public ApiResponse<Integer> backfillRiskWarnings(HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        int processed = diagnosisService.backfillRiskWarnings();
        return ApiResponse.success(processed);
    }
}