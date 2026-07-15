package com.campus.common.vo;

import lombok.Data;

@Data
public class TrendItemVO {
    private String semester;
    private Integer studentCount;
    private Double avgScore;
    private Double passRate;
    private Double maxScore;
    private Double minScore;
}
