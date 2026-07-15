package com.campus.common.vo;

import lombok.Data;

@Data
public class GradeSummaryVO {
    private String grade;
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
