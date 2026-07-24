package com.campus.client.service;

import com.campus.client.model.AiDiagnosis;
import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class DiagnosisService {
    public static PageResult<AiDiagnosis> getPage(int page, int size) throws Exception {
        return ApiClient.get("/diagnoses?page=" + page + "&size=" + size,
                new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static PageResult<AiDiagnosis> getPage(int page, int size, String semester) throws Exception {
        String path = "/diagnoses?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + semester;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static PageResult<AiDiagnosis> getPage(int page, int size, String semester, String keyword) throws Exception {
        String path = "/diagnoses?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + ApiClient.encodeParam(semester);
        }
        if (keyword != null && !keyword.isEmpty()) {
            path += "&keyword=" + ApiClient.encodeParam(keyword);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static PageResult<AiDiagnosis> getMyPage(int page, int size, String semester) throws Exception {
        String path = "/my/diagnosis?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + ApiClient.encodeParam(semester);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static Void generate(int studentId, String semester) throws Exception {
        return ApiClient.post("/diagnoses",
                java.util.Map.of("studentId", studentId, "semester", semester),
                new TypeReference<ApiResponse<Void>>() {});
    }
}
