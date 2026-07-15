package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.AsyncTask;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AsyncTaskService {

    private final ApiService api = new ApiService();

    public ApiResult<AsyncTask> submitTask(String type, Map<String, String> params) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("type", type);
        body.put("params", params);
        String json = api.post("/api/tasks", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<AsyncTask>>() {});
    }

    public ApiResult<AsyncTask> getTaskStatus(String taskId) throws IOException {
        String json = api.get("/api/tasks/" + taskId, null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<AsyncTask>>() {});
    }

    public ApiResult<String> getTaskResult(String taskId) throws IOException {
        String json = api.get("/api/tasks/" + taskId + "/result", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<String>>() {});
    }
}
