package com.campus.common.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RankingItemVO {
    private Integer rank;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long classId;
    private String className;
    private BigDecimal finalScore;
}
