package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StatsService {

    private final ApiService api = new ApiService();

    public ApiResult<Map<String, Object>> getSchoolOverview() throws IOException {
        String json = api.get("/api/stats/school-overview", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getRiskSummary() throws IOException {
        String json = api.get("/api/stats/risk-summary", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getClassStats(Long classId) throws IOException {
        String json = api.get("/api/stats/class/" + classId, null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getScoreDistribution(Long classId, Long courseId) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (classId != null) params.put("classId", String.valueOf(classId));
        if (courseId != null) params.put("courseId", String.valueOf(courseId));
        String json = api.get("/api/stats/score-distribution", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getRanking(Long classId, Long courseId) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (classId != null) params.put("classId", String.valueOf(classId));
        if (courseId != null) params.put("courseId", String.valueOf(courseId));
        String json = api.get("/api/stats/ranking", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getTrend(Long classId, Long courseId) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (classId != null) params.put("classId", String.valueOf(classId));
        if (courseId != null) params.put("courseId", String.valueOf(courseId));
        String json = api.get("/api/stats/trend", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getMyScoreTrend() throws IOException {
        String json = api.get("/api/my/scores/trend", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }

    public ApiResult<Map<String, Object>> getMyScoreRadar() throws IOException {
        String json = api.get("/api/my/scores/radar", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Map<String, Object>>>() {});
    }
}
