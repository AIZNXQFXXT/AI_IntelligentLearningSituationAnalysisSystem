package com.campus.common.vo;

import lombok.Data;

@Data
public class ScoreTrendVO {
    private String semester;
    private Double avgScore;
    private Double maxScore;
    private Double minScore;
    private Integer count;
}
