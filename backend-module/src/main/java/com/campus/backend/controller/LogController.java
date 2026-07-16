package com.campus.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.AICallLog;
import com.campus.backend.entity.OperationLog;
import com.campus.backend.service.LogService;
import com.campus.backend.util.SecurityHelper;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/logs")
@AllArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping("/operation")
    public ApiResponse<PageResult<OperationLog>> pageList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String resultStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        IPage<OperationLog> result = logService.pageList(page, size, username, operation,
                targetType, resultStatus, startDate, endDate);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }

    @GetMapping("/ai-calls")
    public ApiResponse<PageResult<AICallLog>> pageAiCalls(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        SecurityHelper.requireAdmin(request);
        IPage<AICallLog> result = logService.pageAiCalls(page, size);
        return ApiResponse.success(PageResult.of(
                result.getRecords(), result.getTotal(), page, size));
    }
}
