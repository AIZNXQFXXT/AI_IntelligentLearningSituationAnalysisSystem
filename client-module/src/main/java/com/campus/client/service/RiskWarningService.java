package com.campus.client.service;

import com.campus.client.model.ApiResponse;
import com.campus.client.model.PageResult;
import com.campus.client.model.RiskWarning;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiskWarningService {
    public static PageResult<RiskWarning> getRiskWarningPage(int page, int size, String handleStatus) throws Exception {
        StringBuilder params = new StringBuilder("/risk-warnings?page=" + page + "&size=" + size);
        if (handleStatus != null) {
            params.append("&handleStatus=").append(ApiClient.encodeParam(handleStatus));
        }
        return ApiClient.get(params.toString(), new TypeReference<ApiResponse<PageResult<RiskWarning>>>() {});
    }

    public static PageResult<RiskWarning> getPage(int page, int size, String semester, String riskLevel, String handleStatus) throws Exception {
        StringBuilder params = new StringBuilder("/risk-warnings?page=" + page + "&size=" + size);
        if (semester != null) params.append("&semester=").append(ApiClient.encodeParam(semester));
        if (riskLevel != null) params.append("&riskLevel=").append(ApiClient.encodeParam(riskLevel));
        if (handleStatus != null) params.append("&handleStatus=").append(ApiClient.encodeParam(handleStatus));
        return ApiClient.get(params.toString(), new TypeReference<ApiResponse<PageResult<RiskWarning>>>() {});
    }

    public static void handle(int id, String remark) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("handleStatus", "HANDLED");
        body.put("handleRemark", remark);
        ApiClient.put("/risk-warnings/" + id + "/handle", body, new TypeReference<ApiResponse<Void>>() {});
    }

    public static List<RiskWarning> getMyWarnings() throws Exception {
        PageResult<RiskWarning> result = ApiClient.get("/my/warnings",
                new TypeReference<ApiResponse<PageResult<RiskWarning>>>() {});
        return result != null ? result.getRecords() : List.of();
    }
}
