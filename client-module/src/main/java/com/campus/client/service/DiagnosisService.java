package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Diagnosis;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

public class DiagnosisService {

    private final ApiService api = new ApiService();

    public ApiResult<Void> createDiagnosis(Diagnosis diagnosis) throws IOException {
        String json = api.post("/api/diagnoses", diagnosis);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<java.util.List<Diagnosis>> getMyDiagnosis() throws IOException {
        String json = api.get("/api/my/diagnosis", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<java.util.List<Diagnosis>>>() {});
    }
}
