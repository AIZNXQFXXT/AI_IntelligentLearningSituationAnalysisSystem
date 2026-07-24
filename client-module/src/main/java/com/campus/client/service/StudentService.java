package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.client.model.Student;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.Map;

public class StudentService {
    public static PageResult<Student> getPage(int page, int size, String keyword, Integer classId) throws Exception {
        String path = "/students?page=" + page + "&size=" + size;
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + ApiClient.encodeParam(keyword);
        }
        if (classId != null) {
            path += "&classId=" + classId;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<Student>>>() {});
    }

    public static PageResult<Student> getPage(int page, int size) throws Exception {
        return getPage(page, size, null, null);
    }

    public static Void create(Student student) throws Exception {
        return ApiClient.post("/students", student, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, Student student) throws Exception {
        return ApiClient.put("/students/" + id, student, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/students/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Long batchImport(File file) throws Exception {
        Map<String, Long> result = ApiClient.uploadFile("/students/batch", file,
                new TypeReference<ApiResponse<Map<String, Long>>>() {});
        return result != null ? result.get("taskId") : null;
    }

    public static Void toggleStatus(int id, int status) throws Exception {
        return ApiClient.patch("/students/" + id + "/status?status=" + status, null,
                new TypeReference<ApiResponse<Void>>() {});
    }
}
