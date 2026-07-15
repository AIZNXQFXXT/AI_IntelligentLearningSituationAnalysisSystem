package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.User;
import com.campus.client.util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final ApiService api = new ApiService();

    public ApiResult<Map<String, Object>> login(String username, String password) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);
        String json = api.post("/api/auth/login", body);
        ApiResult<Map<String, Object>> result = ApiService.getMapper().readValue(json,
                new TypeReference<ApiResult<Map<String, Object>>>() {});
        if (result.isSuccess() && result.getData() != null) {
            Map<String, Object> data = result.getData();
            String token = (String) data.get("token");
            SessionManager.getInstance().setToken(token);
            if (data.get("userId") != null) {
                SessionManager.getInstance().setUserId(((Number) data.get("userId")).longValue());
            }
            SessionManager.getInstance().setUsername((String) data.get("username"));
            SessionManager.getInstance().setRole((String) data.get("role"));
        }
        return result;
    }

    public ApiResult<Void> logout() throws IOException {
        String json = api.post("/api/auth/logout", null);
        SessionManager.getInstance().clear();
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Map<String, Object>> refresh() throws IOException {
        String json = api.post("/api/auth/refresh", null);
        ApiResult<Map<String, Object>> result = ApiService.getMapper().readValue(json,
                new TypeReference<ApiResult<Map<String, Object>>>() {});
        if (result.isSuccess() && result.getData() != null) {
            String token = (String) result.getData().get("token");
            if (token != null) {
                SessionManager.getInstance().setToken(token);
            }
        }
        return result;
    }

    public ApiResult<Void> updateProfile(Map<String, String> fields) throws IOException {
        String json = api.patch("/api/auth/profile", fields);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> changePassword(String oldPassword, String newPassword) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("oldPassword", oldPassword);
        body.put("newPassword", newPassword);
        String json = api.put("/api/auth/password", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
