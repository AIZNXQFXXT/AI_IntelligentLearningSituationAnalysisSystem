package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Course;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CourseService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<Course>> getCourses(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/courses", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Course>>>() {});
    }

    public ApiResult<Void> createCourse(Course course) throws IOException {
        String json = api.post("/api/courses", course);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateCourse(Long id, Course course) throws IOException {
        String json = api.put("/api/courses/" + id, course);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateCourseStatus(Long id, Integer status) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        String json = api.patch("/api/courses/" + id + "/status", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
