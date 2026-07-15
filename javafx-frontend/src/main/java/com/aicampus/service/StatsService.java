package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.ScoreDistribution;
import com.aicampus.model.SchoolOverview;
import com.aicampus.model.SchoolStats;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

public class StatsService {
    public static SchoolOverview getSchoolOverview() throws Exception {
        return ApiClient.get("/stats/overview", new TypeReference<ApiResponse<SchoolOverview>>() {});
    }

    public static List<ScoreDistribution> getScoreDistribution(int classId, int courseId) throws Exception {
        return ApiClient.get("/stats/class/" + classId + "/course/" + courseId + "/distribution",
                new TypeReference<ApiResponse<List<ScoreDistribution>>>() {});
    }

    public static SchoolStats getClassStats(int classId, int courseId) throws Exception {
        return ApiClient.get("/stats/class/" + classId + "/course/" + courseId,
                new TypeReference<ApiResponse<SchoolStats>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getTrend(int classId) throws Exception {
        return ApiClient.get("/stats/class/" + classId + "/trend",
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getStudentTrend(int studentId) throws Exception {
        return ApiClient.get("/stats/student/" + studentId + "/trend",
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getStudentRadar(int studentId) throws Exception {
        return ApiClient.get("/stats/student/" + studentId + "/radar",
                new TypeReference<ApiResponse<List<Map<String, Object>>>>() {});
    }
}
