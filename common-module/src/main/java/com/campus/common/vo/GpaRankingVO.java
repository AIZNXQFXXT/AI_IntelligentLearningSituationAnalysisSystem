package com.campus.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GpaRankingVO {
    private Integer rank;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String className;
    private Double gpa;
    private Integer courseCount;
}
