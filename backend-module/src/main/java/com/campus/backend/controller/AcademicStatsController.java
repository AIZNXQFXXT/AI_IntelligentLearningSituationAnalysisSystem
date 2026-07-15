package com.campus.backend.controller;

import com.campus.backend.service.AcademicStatsService;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.CourseSummaryVO;
import com.campus.common.vo.GradeSummaryVO;
import com.campus.common.vo.RiskDistributionVO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/academic-stats")
@AllArgsConstructor
public class AcademicStatsController {

    private final AcademicStatsService academicStatsService;

    @GetMapping("/grade-summary")
    public ApiResponse<List<GradeSummaryVO>> getGradeSummary(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.success(academicStatsService.getGradeSummary(grade, courseId));
    }

    @GetMapping("/course-summary")
    public ApiResponse<List<CourseSummaryVO>> getCourseSummary(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.success(academicStatsService.getCourseSummary(grade, courseId));
    }

    @GetMapping("/risk-distribution")
    public ApiResponse<List<RiskDistributionVO>> getRiskDistribution(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false, defaultValue = "grade") String groupBy) {
        return ApiResponse.success(academicStatsService.getRiskDistribution(grade, groupBy));
    }
}
