package com.aicampus.service;

import com.aicampus.model.AiCallLog;
import com.aicampus.model.ApiResponse;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class AiCallLogService {
    public static PageResult<AiCallLog> getPage(int page, int size, String functionName, Boolean success) throws Exception {
        String path = "/ai-call-logs?page=" + page + "&size=" + size;
        if (functionName != null && !functionName.isEmpty()) path += "&functionName=" + functionName;
        if (success != null) path += "&success=" + (success ? "1" : "0");
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<AiCallLog>>>() {});
    }
}
