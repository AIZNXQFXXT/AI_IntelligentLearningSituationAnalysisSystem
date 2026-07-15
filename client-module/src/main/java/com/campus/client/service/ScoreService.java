package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Score;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ScoreService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<Score>> getScores(int page, int size, Long examId, Long courseId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (examId != null) params.put("examId", String.valueOf(examId));
        if (courseId != null) params.put("courseId", String.valueOf(courseId));
        String json = api.get("/api/scores", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Score>>>() {});
    }

    public ApiResult<Void> submitScore(Score score) throws IOException {
        String json = api.post("/api/scores", score);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateScore(Long id, Score score) throws IOException {
        String json = api.put("/api/scores/" + id, score);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> batchSubmitScores(java.util.List<Score> scores) throws IOException {
        String json = api.post("/api/scores/batch", scores);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<PageResult<Score>> getArchiveOverview(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/scores/archive/overview", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Score>>>() {});
    }

    public ApiResult<Void> updateScoreStatus(Long id, Integer status) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        String json = api.put("/api/scores/" + id + "/status", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
