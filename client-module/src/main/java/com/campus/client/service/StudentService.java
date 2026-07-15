package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StudentService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<Student>> getStudents(int page, int size, Long classId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (classId != null) params.put("classId", String.valueOf(classId));
        String json = api.get("/api/students", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Student>>>() {});
    }

    public ApiResult<Void> createStudent(Student student) throws IOException {
        String json = api.post("/api/students", student);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> batchImport(byte[] fileData) throws IOException {
        String json = api.post("/api/students/batch", fileData);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateStudent(Long id, Student student) throws IOException {
        String json = api.put("/api/students/" + id, student);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateStudentStatus(Long id, Integer status) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        String json = api.patch("/api/students/" + id + "/status", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<PageResult<Student>> getMyClassStudents(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/students/my-class", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Student>>>() {});
    }
}
