package com.campus.common.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRadarVO {
    private String courseName;
    private BigDecimal finalScore;
}
