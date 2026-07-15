package com.campus.common.vo;

import lombok.Data;

@Data
public class ClassStatsVO {
    private Long classId;
    private String className;
    private String courseName;
    private Integer totalStudents;
    private Integer scoredStudents;
    private Double avgScore;
    private Double passRate;
    private Double maxScore;
    private Double minScore;
    private Double medianScore;
    private Integer excellentCount;
    private Integer goodCount;
    private Integer mediumCount;
    private Integer passCount;
    private Integer failCount;
}
