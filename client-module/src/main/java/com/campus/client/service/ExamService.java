package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Exam;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExamService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<Exam>> getExams(int page, int size) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        String json = api.get("/api/exams", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Exam>>>() {});
    }

    public ApiResult<Void> createExam(Exam exam) throws IOException {
        String json = api.post("/api/exams", exam);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateExam(Long id, Exam exam) throws IOException {
        String json = api.put("/api/exams/" + id, exam);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> deleteExam(Long id) throws IOException {
        String json = api.delete("/api/exams/" + id);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
