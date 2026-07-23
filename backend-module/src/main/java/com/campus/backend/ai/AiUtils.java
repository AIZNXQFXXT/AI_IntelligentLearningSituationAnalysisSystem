package com.campus.backend.ai;

import java.util.Locale;

public class AiUtils {

    public static String extractJsonContent(String raw) {
        if (raw == null) return "";
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            return raw.substring(start, end + 1).trim();
        }
        return raw.trim();
    }

    /**
     * 归一化 AI 返回的 riskLevel 字段，覆盖中文/英文/大小写/带修饰词等变体。
     * 匹配失败返回 null，调用方自行决定回退策略。
     */
    public static String normalizeRiskLevel(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) return null;
        if (s.contains("HIGH") || s.equals("高") || s.contains("高风险") || s.contains("严重")) return "HIGH";
        if (s.contains("MEDIUM") || s.equals("中") || s.contains("中风险") || s.contains("中等")) return "MEDIUM";
        if (s.contains("LOW") || s.equals("低") || s.contains("低风险")) return "LOW";
        if (s.contains("NONE") || s.contains("无") || s.contains("N/A") || s.equals("NA")) return null;
        return null;
    }

}
