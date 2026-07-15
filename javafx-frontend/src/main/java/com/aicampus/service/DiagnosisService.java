package com.aicampus.service;

import com.aicampus.model.AiDiagnosis;
import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class DiagnosisService {
    public static PageResult<AiDiagnosis> getPage(int page, int size) throws Exception {
        return ApiClient.get("/ai-diagnoses?page=" + page + "&size=" + size,
                new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static PageResult<AiDiagnosis> getPage(int page, int size, String semester) throws Exception {
        String path = "/ai-diagnoses?page=" + page + "&size=" + size;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + semester;
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiDiagnosis>>>() {});
    }

    public static Void generate(int studentId, String semester) throws Exception {
        return ApiClient.post("/ai-diagnoses/generate",
                java.util.Map.of("studentId", studentId, "semester", semester),
                new TypeReference<ApiResponse<Void>>() {});
    }
}
