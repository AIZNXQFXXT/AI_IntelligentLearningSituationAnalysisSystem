package com.campus.common.vo;

import lombok.Data;

@Data
public class RiskSummaryVO {
    private long totalWarnings;
    private long highRiskCount;
    private long mediumRiskCount;
    private long lowRiskCount;
    private long handledCount;
    private long unhandledCount;
}
