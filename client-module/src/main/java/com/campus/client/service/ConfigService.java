package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.SysConfig;
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
