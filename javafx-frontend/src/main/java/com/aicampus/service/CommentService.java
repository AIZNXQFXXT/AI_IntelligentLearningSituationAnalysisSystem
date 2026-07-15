package com.aicampus.service;

import com.aicampus.model.AiComment;
import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class CommentService {
    public static PageResult<AiComment> getPage(int page, int size) throws Exception {
        String path = "/ai-comments?page=" + page + "&size=" + size;
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiComment>>>() {});
    }
}
