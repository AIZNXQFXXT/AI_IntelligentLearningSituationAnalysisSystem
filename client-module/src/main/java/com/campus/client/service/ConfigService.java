package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.SysConfig;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

public class ConfigService {
    public static List<SysConfig> getAll() throws Exception {
        return ApiClient.get("/system/configs", new TypeReference<ApiResponse<List<SysConfig>>>() {});
    }

    public static Void update(int id, SysConfig config) throws Exception {
        return ApiClient.put("/system/configs/" + id, config, new TypeReference<ApiResponse<Void>>() {});
    }
}
