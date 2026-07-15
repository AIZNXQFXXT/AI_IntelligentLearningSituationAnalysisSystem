package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Suggestion;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

public class SuggestionService {

    private final ApiService api = new ApiService();

    public ApiResult<List<Suggestion>> getSuggestions(Long studentId) throws IOException {
        String path = studentId != null ? "/api/suggestions?studentId=" + studentId : "/api/suggestions";
        String json = api.get(path, null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<List<Suggestion>>>() {});
    }

    public ApiResult<List<Suggestion>> getMySuggestions() throws IOException {
        String json = api.get("/api/my/suggestions", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<List<Suggestion>>>() {});
    }
}
