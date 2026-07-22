package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.ClassInfo;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.List;

public class ClassService {
    public static PageResult<ClassInfo> getPage(int page, int size, String keyword) throws Exception {
        String path = "/classes?page=" + page + "&size=" + size;
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + ApiClient.encodeParam(keyword);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<ClassInfo>>>() {});
    }

    public static List<ClassInfo> getAll() throws Exception {
        return ApiClient.get("/classes/list", new TypeReference<ApiResponse<List<ClassInfo>>>() {});
    }

    public static List<ClassInfo> getMyClasses() throws Exception {
        return ApiClient.get("/classes/my", new TypeReference<ApiResponse<List<ClassInfo>>>() {});
    }

    public static Void create(ClassInfo cls) throws Exception {
        return ApiClient.post("/classes", cls, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, ClassInfo cls) throws Exception {
        return ApiClient.put("/classes/" + id, cls, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/classes/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void batchImport(File file) throws Exception {
        return ApiClient.uploadFile("/classes/batch", file, new TypeReference<ApiResponse<Void>>() {});
    }
}
