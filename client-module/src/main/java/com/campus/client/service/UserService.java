package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.PageResult;
import com.campus.client.model.User;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UserService {

    private final ApiService api = new ApiService();

    public ApiResult<PageResult<User>> getUsers(int page, int size, String role) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (role != null && !role.isEmpty()) params.put("role", role);
        String json = api.get("/api/users", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<User>>>() {});
    }

    public ApiResult<Void> createUser(User user) throws IOException {
        String json = api.post("/api/users", user);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> updateUserStatus(Long id, Integer status) throws IOException {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        String json = api.put("/api/users/" + id + "/status", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<Void> resetPassword(Long id, String newPassword) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("password", newPassword);
        String json = api.put("/api/users/" + id + "/password", body);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }
}
