package com.aicampus.service;

import com.aicampus.model.ApiResponse;
import com.aicampus.model.OperationLog;
import com.aicampus.model.PageResult;
import com.fasterxml.jackson.core.type.TypeReference;

public class OperationLogService {
    public static PageResult<OperationLog> getPage(int page, int size, String username,
            String operation, String startDate, String endDate) throws Exception {
        String path = "/operation-logs?page=" + page + "&size=" + size;
        if (username != null && !username.isEmpty()) path += "&username=" + username;
        if (operation != null && !operation.isEmpty()) path += "&operation=" + operation;
        if (startDate != null && !startDate.isEmpty()) path += "&startDate=" + startDate;
        if (endDate != null && !endDate.isEmpty()) path += "&endDate=" + endDate;
        return ApiClient.get(path, new TypeReference<ApiResponse<PageResult<OperationLog>>>() {});
    }
}
