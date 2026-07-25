package com.campus.client.util;

import com.campus.client.service.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonContentExtractor {

    /**
     * 从评语内容中提取可显示文本。
     * AI 应返回纯文本（prompt 要求不要JSON包裹），但部分模型仍可能返回JSON。
     * - 若为JSON，依次提取 comment / content / text 字段
     * - 否则原样返回
     */
    public static String extractCommentContent(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String trimmed = raw.trim();
        if (!trimmed.startsWith("{")) return trimmed;
        try {
            JsonNode json = ApiClient.getMapper().readTree(trimmed);
            if (json.has("comment")) return json.get("comment").asText();
            if (json.has("content")) return json.get("content").asText();
            if (json.has("text")) return json.get("text").asText();
        } catch (Exception ignored) {}
        return trimmed;
    }

    /**
     * 为诊断表格预览提取摘要，当 JsonFormatter.parseDiagnosisReport 失败时降级使用。
     * 尝试从诊断JSON中提取 overall 字段，失败则返回清理后的纯文本。
     */
    public static String extractDiagnosisPreview(String diagnosisText) {
        if (diagnosisText == null || diagnosisText.isBlank()) return "";
        String trimmed = diagnosisText.trim();
        try {
            JsonNode json = ApiClient.getMapper().readTree(trimmed);
            if (json.has("overall") && !json.get("overall").asText().isBlank()) {
                return json.get("overall").asText();
            }
        } catch (Exception ignored) {}
        if (trimmed.startsWith("{")) {
            int idx = trimmed.indexOf("\"overall\"");
            if (idx != -1) {
                int start = trimmed.indexOf('"', idx + 9);
                if (start != -1) {
                    int end = trimmed.indexOf('"', start + 1);
                    if (end != -1) {
                        return trimmed.substring(start + 1, end);
                    }
                }
            }
        }
        return trimmed;
    }

    /**
     * 通用内容提取：若为JSON，按优先级提取 comment / content / text 字段。
     */
    public static String extractContent(String raw) {
        return extractCommentContent(raw);
    }
}
