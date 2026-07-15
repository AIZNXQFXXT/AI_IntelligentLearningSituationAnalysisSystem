package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.LogEntry;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<LogEntry>> getOperationLogs(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/logs/operation", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<LogEntry>>>() {});
    }

    public ApiResult<PageResult<LogEntry>> getAiCallLogs(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/logs/ai-calls", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<LogEntry>>>() {});
    }
}
