package com.campus.common.vo;

import lombok.Data;

@Data
public class SchoolOverviewVO {
    private long totalStudents;
    private long totalClasses;
    private long totalCourses;
    private long totalExams;
    private double avgScore;
    private double passRate;
    private double failRate;
    private long excellentCount;
    private long goodCount;
    private long mediumCount;
    private long passCount;
    private long failCount;
}
