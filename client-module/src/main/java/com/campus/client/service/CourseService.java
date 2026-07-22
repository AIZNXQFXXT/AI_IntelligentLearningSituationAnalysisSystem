package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.common.dto.CourseDTO;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.util.List;

public class CourseService {
    public static PageResult<CourseDTO> getPage(int page, int size, String keyword, String type) throws Exception {
        String path = "/courses?page=" + page + "&size=" + size;
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + ApiClient.encodeParam(keyword);
        }
        if (type != null && !type.isEmpty()) {
            path += "&type=" + ApiClient.encodeParam(type);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<CourseDTO>>>() {});
    }

    public static List<CourseDTO> getAll() throws Exception {
        return ApiClient.get("/courses/list", new TypeReference<ApiResponse<List<CourseDTO>>>() {});
    }

    public static Void create(CourseDTO course) throws Exception {
        return ApiClient.post("/courses", course, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, CourseDTO course) throws Exception {
        return ApiClient.put("/courses/" + id, course, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/courses/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void batchImport(File file) throws Exception {
        return ApiClient.uploadFile("/courses/batch", file, new TypeReference<ApiResponse<Void>>() {});
    }
}
