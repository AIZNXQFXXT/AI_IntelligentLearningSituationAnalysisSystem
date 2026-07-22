package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.client.model.Teacher;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.List;

public class TeacherService {
    public static PageResult<Teacher> getPage(int page, int size, String keyword) throws Exception {
        String path = "/teachers?page=" + page + "&size=" + size;
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + ApiClient.encodeParam(keyword);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<Teacher>>>() {});
    }

    public static List<Teacher> getAll() throws Exception {
        return ApiClient.get("/teachers?page=1&size=999", new TypeReference<ApiResponse<PageResult<Teacher>>>() {}).getRecords();
    }

    public static Void create(Teacher teacher) throws Exception {
        return ApiClient.post("/teachers", teacher, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, Teacher teacher) throws Exception {
        return ApiClient.put("/teachers/" + id, teacher, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/teachers/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void batchImport(File file) throws Exception {
        return ApiClient.uploadFile("/teachers/batch", file, new TypeReference<ApiResponse<Void>>() {});
    }
}
