package com.campus.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScoreDistributionVO {
    private String rangeLabel;
    private Integer count;
    private Double percentage;
}
