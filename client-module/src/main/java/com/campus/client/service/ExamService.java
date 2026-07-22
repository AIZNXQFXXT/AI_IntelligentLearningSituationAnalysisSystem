package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.common.dto.ExamDTO;
import com.fasterxml.jackson.core.type.TypeReference;

public class ExamService {
    public static PageResult<ExamDTO> getPage(int page, int size, String semester) throws Exception {
        String path = "/exams?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + semester;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<ExamDTO>>>() {});
    }

    public static Void create(ExamDTO exam) throws Exception {
        return ApiClient.post("/exams", exam, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void update(int id, ExamDTO exam) throws Exception {
        return ApiClient.put("/exams/" + id, exam, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void delete(int id) throws Exception {
        return ApiClient.delete("/exams/" + id, new TypeReference<ApiResponse<Void>>() {});
    }

    public static Void archive(int id) throws Exception {
        return ApiClient.patch("/exams/" + id + "/archive", null, new TypeReference<ApiResponse<Void>>() {});
    }
}
