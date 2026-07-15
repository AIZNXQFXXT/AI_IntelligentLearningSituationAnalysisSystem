package com.campus.common.vo;

import lombok.Data;

@Data
public class RiskDistributionVO {
    private String dimension;
    private String dimensionValue;
    private int highRiskCount;
    private int mediumRiskCount;
    private int lowRiskCount;
    private int totalCount;
}
