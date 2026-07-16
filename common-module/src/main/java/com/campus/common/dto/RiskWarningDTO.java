package com.campus.common.dto;

import lombok.Data;

@Data
public class RiskWarningDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String className;
    private String semester;
    private String riskLevel;
    private String riskReason;
    private String aiAnalysis;
    private String handleStatus;
    private String handleRemark;
}