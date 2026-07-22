package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.ScoreDistribution;
import com.campus.client.model.SchoolOverview;
import com.campus.client.model.SchoolOverviewVO;
import com.campus.client.model.SchoolStats;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class StatsService {
    public static SchoolOverview getSchoolOverview() throws Exception {
        return ApiClient.get("/stats/overview", new TypeReference<ApiResponse<SchoolOverview>>() {});
    }

    public static SchoolOverviewVO getSchoolAcademicOverview() throws Exception {
        return ApiClient.get("/stats/school-overview", new TypeReference<ApiResponse<SchoolOverviewVO>>() {});
    }

    public static List<ScoreDistribution> getScoreDistribution(int classId, int courseId) throws Exception {
        return ApiClient.get("/stats/score-distribution?classId=" + classId + "&courseId=" + courseId,
                new TypeReference<ApiResponse<List<ScoreDistribution>>>() {});
    }

    public static SchoolStats getClassStats(int classId, int courseId) throws Exception {
        return ApiClient.get("/stats/class/" + classId + "?courseId=" + courseId,
                new TypeReference<ApiResponse<SchoolStats>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTrend(int classId, int courseId) throws Exception {
        return ApiClient.get("/stats/trend?classId=" + classId + "&courseId=" + courseId,
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getStudentTrend() throws Exception {
        return ApiClient.get("/my/scores/trend",
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getStudentRadar(String semester) throws Exception {
        return ApiClient.get("/my/scores/radar?semester=" + ApiClient.encodeParam(semester),
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getRiskSummary() throws Exception {
        return ApiClient.get("/stats/risk-summary",
                new TypeReference<ApiResponse<Map<String, Object>>>() {});
    }
}
