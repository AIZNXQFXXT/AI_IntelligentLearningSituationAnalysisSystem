package com.campus.client.util;

import com.campus.client.model.DiagnosisReport;
import com.campus.client.service.ApiClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonFormatter {

    private static final DiagnosisReport EMPTY = new DiagnosisReport();

    public static DiagnosisReport parseDiagnosisReport(String json) {
        if (json == null || json.isBlank()) return EMPTY;
        try {
            return ApiClient.getMapper().readValue(json, DiagnosisReport.class);
        } catch (Exception e) {
            DiagnosisReport fallback = new DiagnosisReport();
            fallback.setOverall(json);
            return fallback;
        }
    }

    public static String formatReport(DiagnosisReport report) {
        if (report == null) return "";
        StringBuilder sb = new StringBuilder();

        if (report.getOverall() != null && !report.getOverall().isBlank()) {
            sb.append("=== 总体评价 ===\n").append(report.getOverall()).append("\n\n");
        }
        if (report.getStrengths() != null && !report.getStrengths().isEmpty()) {
            sb.append("=== 优势 ===\n").append(formatKeyValueList(report.getStrengths(), "科目", "描述")).append("\n\n");
        }
        if (report.getWeaknesses() != null && !report.getWeaknesses().isEmpty()) {
            sb.append("=== 劣势 ===\n").append(formatKeyValueList(report.getWeaknesses(), "科目", "描述")).append("\n\n");
        }
        if (report.getTrend() != null && !report.getTrend().isBlank()) {
            sb.append("=== 趋势分析 ===\n").append(report.getTrend()).append("\n\n");
        }
        if (report.getSuggestions() != null && !report.getSuggestions().isEmpty()) {
            sb.append("=== 学习建议 ===\n").append(formatNumberedList(report.getSuggestions())).append("\n\n");
        }
        if (report.getRiskLevel() != null && !report.getRiskLevel().isBlank()) {
            sb.append("=== 风险等级 ===\n").append(formatRiskLevel(report.getRiskLevel()));
        }

        return sb.toString();
    }

    public static String formatKeyValueList(List<Map<String, String>> items, String keyLabel, String valueLabel) {
        if (items == null || items.isEmpty()) return "暂无";
        return items.stream()
                .map(item -> {
                    String key = item.getOrDefault("subject", item.getOrDefault("name", ""));
                    String value = item.getOrDefault("reason", item.getOrDefault("desc", ""));
                    return key + "：" + value;
                })
                .collect(Collectors.joining("\n"));
    }

    public static String formatNumberedList(List<String> items) {
        if (items == null || items.isEmpty()) return "暂无";
        return IntStream.range(0, items.size())
                .mapToObj(i -> (i + 1) + ". " + items.get(i))
                .collect(Collectors.joining("\n"));
    }

    public static String formatRiskLevel(String level) {
        if (level == null) return "未知";
        switch (level.toUpperCase()) {
            case "HIGH":   return "高风险";
            case "MEDIUM": return "中风险";
            case "LOW":    return "低风险";
            default:       return level;
        }
    }
}
