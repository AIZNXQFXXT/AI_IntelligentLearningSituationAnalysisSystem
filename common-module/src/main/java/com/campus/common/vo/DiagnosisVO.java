package com.campus.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiagnosisVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String className;
    private String semester;
    private String overall;
    private List<StrengthItem> strengths;
    private List<WeaknessItem> weaknesses;
    private String trend;
    private List<String> suggestions;
    private String riskLevel;
    private String diagnosisText;
    private String aiModel;
    private Integer tokensUsed;
    private LocalDateTime createdAt;

    @Data
    public static class StrengthItem {
        private String subject;
        private String reason;
    }

    @Data
    public static class WeaknessItem {
        private String subject;
        private String reason;
    }
}