package com.campus.common.vo;

import lombok.Data;

@Data
public class CourseSummaryVO {
    private Long courseId;
    private String courseName;
    private int totalStudents;
    private int scoredStudents;
    private double avgScore;
    private double passRate;
    private int failCount;
    private int excellentCount;
    private int goodCount;
    private int mediumCount;
    private int passCount;
}
