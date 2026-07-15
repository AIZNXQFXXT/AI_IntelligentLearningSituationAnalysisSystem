package com.campus.backend.controller;

import com.campus.backend.service.StatsService;
import com.campus.common.vo.ApiResponse;
import com.campus.common.vo.ClassStatsVO;
import com.campus.common.vo.RankingItemVO;
import com.campus.common.vo.ScoreDistributionVO;
import com.campus.common.vo.TrendItemVO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
@AllArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/class/{classId}")
    public ApiResponse<ClassStatsVO> getClassStats(
            @PathVariable Long classId,
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.success(statsService.getClassStats(classId, examId, courseId));
    }

    @GetMapping("/score-distribution")
    public ApiResponse<List<ScoreDistributionVO>> getScoreDistribution(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId) {
        return ApiResponse.success(statsService.getScoreDistribution(examId, courseId, classId));
    }

    @GetMapping("/ranking")
    public ApiResponse<List<RankingItemVO>> getRanking(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long classId) {
        return ApiResponse.success(statsService.getRanking(examId, courseId, classId));
    }

    @GetMapping("/trend")
    public ApiResponse<List<TrendItemVO>> getTrend(
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.success(statsService.getTrend(classId, courseId));
    }
}
