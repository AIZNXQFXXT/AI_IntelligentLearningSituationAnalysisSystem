package com.campus.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseGradeVO {
    private String courseName;
    private Double avgScore;     // 保留 2 位
    private Double maxScore;
    private Double minScore;
    private Integer studentCount;
}
