package com.campus.backend.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AIDiagnosisRecord {
    private int id;
    private int studentId;
    private String semester;
    private String diagnosisText;
    private String strengths;
    private String weaknesses;
    private String trendAnalysis;
    private String riskLevel;
    private int tokensUsed;
    private double cost;
    private int durationMs;
    private String aiModel;
    private String promptTemplate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
