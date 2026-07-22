package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.LoginResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class AuthService {
    public static LoginResult login(String username, String password) throws Exception {
        Map<String, String> body = Map.of("username", username, "password", password);
        return ApiClient.post("/auth/login", body, new TypeReference<ApiResponse<LoginResult>>() {});
    }
}
