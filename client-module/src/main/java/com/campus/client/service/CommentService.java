package com.campus.client.service;

import com.campus.client.model.ApiResult;
import com.campus.client.model.Comment;
import com.campus.client.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentService {

    private final ApiService api = new ApiService();

    public ApiResult<Void> batchCreateComments(List<Comment> comments) throws IOException {
        String json = api.post("/api/comments/batch", comments);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<PageResult<Comment>> getComments(int page, int size, Long studentId) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("size", String.valueOf(size));
        if (studentId != null) params.put("studentId", String.valueOf(studentId));
        String json = api.get("/api/comments", params);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<PageResult<Comment>>>() {});
    }

    public ApiResult<Void> updateComment(Long id, Comment comment) throws IOException {
        String json = api.put("/api/comments/" + id, comment);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<Void>>() {});
    }

    public ApiResult<List<Comment>> getMyComments() throws IOException {
        String json = api.get("/api/my/comments", null);
        return ApiService.getMapper().readValue(json, new TypeReference<ApiResult<List<Comment>>>() {});
    }
}
