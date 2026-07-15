package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Dict;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

public class DictService {

    private final ApiService api = new ApiService();

    public ApiResult<List<Dict>> getDicts() throws IOException {
        String json = api.get("/api/system/dicts", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<List<Dict>>>() {});
    }

    public ApiResult<Void> createDict(Dict dict) throws IOException {
        String json = api.post("/api/system/dicts", dict);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateDict(Long id, Dict dict) throws IOException {
        String json = api.put("/api/system/dicts/" + id, dict);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
