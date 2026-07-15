package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.ClassInfo;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ClassService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<ClassInfo>> getClasses(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/classes", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<ClassInfo>>>() {});
    }

    public ApiResult<Void> createClass(ClassInfo classInfo) throws IOException {
        String json = api.post("/api/classes", classInfo);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateClass(Long id, ClassInfo classInfo) throws IOException {
        String json = api.put("/api/classes/" + id, classInfo);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> deleteClass(Long id) throws IOException {
        String json = api.delete("/api/classes/" + id);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public byte[] exportExcel() throws IOException {
        return api.getBytes("/api/classes/export/excel");
    }

    public ApiResult<PageResult<ClassInfo>> getMyClasses(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/classes/my", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<ClassInfo>>>() {});
    }
}
