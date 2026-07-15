package com.campus.backend.controller;

import com.campus.backend.service.DashboardService;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.DashboardVO;
import com.campus.common.vo.RiskSummaryVO;
import com.campus.common.vo.SchoolOverviewVO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats/overview")
    public ApiResponse<DashboardVO> getAdminDashboard() {
        return ApiResponse.success(dashboardService.getAdminDashboard());
    }

    @GetMapping("/stats/school-overview")
    public ApiResponse<SchoolOverviewVO> getSchoolOverview() {
        return ApiResponse.success(dashboardService.getSchoolOverview());
    }

    @GetMapping("/stats/risk-summary")
    public ApiResponse<RiskSummaryVO> getRiskSummary() {
        return ApiResponse.success(dashboardService.getRiskSummary());
    }
}
