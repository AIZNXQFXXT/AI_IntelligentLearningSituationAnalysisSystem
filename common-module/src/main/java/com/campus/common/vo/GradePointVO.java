package com.campus.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradePointVO {
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Double gpa;          // 0.0 ~ 5.0，四舍五入保留 2 位
    private Integer courseCount; // 参与计算的课数
}
