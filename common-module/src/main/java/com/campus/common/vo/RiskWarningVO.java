package com.campus.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RiskWarningVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private String semester;
    private String riskLevel;
    private String riskReason;
    private String aiAnalysis;
    private String handleStatus;
    private String handlerName;
    private String handleRemark;
    private LocalDateTime handleAt;
    private LocalDateTime createdAt;
    private List<String> recommendations;
}