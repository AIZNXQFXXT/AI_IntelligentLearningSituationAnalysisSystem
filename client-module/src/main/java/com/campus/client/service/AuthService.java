package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.LoginResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class AuthService {
    public static LoginResult login(String username, String password) throws Exception {
        Map<String, String> body = Map.of("username", username, "password", password);
        return ApiClient.post("/auth/login", body, new TypeReference<ApiResponse<LoginResult>>() {});
    }
}
