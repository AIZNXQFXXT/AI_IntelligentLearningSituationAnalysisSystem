package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.SystemConfig;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

public class SystemConfigService {

    private final ApiService api = new ApiService();

    public ApiResult<List<SystemConfig>> getConfigs() throws IOException {
        String json = api.get("/api/system/configs", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<List<SystemConfig>>>() {});
    }

    public ApiResult<Void> updateConfigs(List<SystemConfig> configs) throws IOException {
        String json = api.put("/api/system/configs", configs);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
