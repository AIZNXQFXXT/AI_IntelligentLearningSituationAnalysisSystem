package com.campus.backend.eneity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RiskWarning {
    private int id;
    private int studentId;
    private String semester;
    private String riskLevel;
    private String riskReason;
    private String aiAnalysis;
    private String handleStatus;
    private int handlerId;
    private String handleRemark;
    private LocalDateTime handleAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
}
