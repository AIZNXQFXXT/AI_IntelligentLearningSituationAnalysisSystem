package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.RiskWarning;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RiskWarningService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<RiskWarning>> getRiskWarnings(int page, int size, Integer handled) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (handled != null) params.put("handled", String.valueOf(handled));
        String json = api.get("/api/risk-warnings", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<RiskWarning>>>() {});
    }

    public ApiResult<Void> handleWarning(Long id, String result) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("handleResult", result);
        String json = api.put("/api/risk-warnings/" + id + "/handle", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<java.util.List<RiskWarning>> getMyWarnings() throws IOException {
        String json = api.get("/api/my/warnings", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<java.util.List<RiskWarning>>>() {});
    }
}
