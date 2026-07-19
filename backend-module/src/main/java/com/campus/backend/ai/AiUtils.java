package com.campus.backend.ai;

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

}
