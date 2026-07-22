package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.Exam;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class ExamService {
    public static PageResult<Exam> getPage(int page, int size, String semester) throws Exception {
        String path = "/exams?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + semester;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<Exam>>>() {});
    }

    public static Void create(Exam exam) throws Exception {
        return ApiClient.post("/exams", exam, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, Exam exam) throws Exception {
        return ApiClient.put("/exams/" + id, exam, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/exams/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void archive(int id) throws Exception {
        return ApiClient.patch("/exams/" + id + "/archive", null, new TypeReference<ApiResponse<Void>>() {});
    }
}
