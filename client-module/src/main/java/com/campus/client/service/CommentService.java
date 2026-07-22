package com.aicampus.service;

import com.aicampus.model.AiComment;
import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class CommentService {
    public static PageResult<AiComment> getPage(int page, int size) throws Exception {
        String path = "/comments?page=" + page + "&size=" + size;
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiComment>>>() {});
    }

    public static PageResult<AiComment> getPage(int page, int size, int classId, String semester) throws Exception {
        String path = "/comments?page=" + page + "&size=" + size + "&classId=" + classId;
        if (semester != null && !semester.isEmpty()) {
            path += "&semester=" + ApiClient.encodeParam(semester);
        }
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiComment>>>() {});
    }

    public static AiComment updateComment(int id, String content) throws Exception {
        return ApiClient.put("/comments/" + id, Map.of("content", content),
                new TypeReference<ApiResponse<AiComment>>() {});
    }

    public static Void batchGenerate(int classId, String semester) throws Exception {
        return ApiClient.post("/comments/batch", Map.of("classId", classId, "semester", semester),
                new TypeReference<ApiResponse<Void>>() {});
    }

    public static PageResult<AiComment> getMyPage(int page, int size) throws Exception {
        String path = "/my/comments?page=" + page + "&size=" + size;
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiComment>>>() {});
    }
}
