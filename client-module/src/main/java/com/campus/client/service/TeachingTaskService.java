package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.TeachingTask;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeachingTaskService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<TeachingTask>> getTasks(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/teaching-tasks", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<TeachingTask>>>() {});
    }

    public ApiResult<Void> createTask(TeachingTask task) throws IOException {
        String json = api.post("/api/teaching-tasks", task);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> deleteTask(Long id) throws IOException {
        String json = api.delete("/api/teaching-tasks/" + id);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
