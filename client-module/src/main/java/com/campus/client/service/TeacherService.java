package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Teacher;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TeacherService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<Teacher>> getTeachers(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/teachers", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Teacher>>>() {});
    }

    public ApiResult<Void> createTeacher(Teacher teacher) throws IOException {
        String json = api.post("/api/teachers", teacher);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> batchImport(byte[] fileData) throws IOException {
        String json = api.post("/api/teachers/batch", fileData);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateTeacher(Long id, Teacher teacher) throws IOException {
        String json = api.put("/api/teachers/" + id, teacher);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateTeacherStatus(Long id, Integer status) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        String json = api.patch("/api/teachers/" + id + "/status", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
