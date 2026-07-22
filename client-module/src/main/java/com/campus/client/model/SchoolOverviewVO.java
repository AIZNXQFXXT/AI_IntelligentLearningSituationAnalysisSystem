package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchoolOverviewVO {
    @JsonProperty("totalStudents")
    private long totalStudents;
    @JsonProperty("totalClasses")
    private long totalClasses;
    @JsonProperty("totalCourses")
    private long totalCourses;
    @JsonProperty("totalExams")
    private long totalExams;
    @JsonProperty("avgScore")
    private double avgScore;
    @JsonProperty("passRate")
    private double passRate;
    @JsonProperty("failRate")
    private double failRate;
    @JsonProperty("excellentCount")
    private long excellentCount;

    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalClasses() { return totalClasses; }
    public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }
    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }
    public long getTotalExams() { return totalExams; }
    public void setTotalExams(long totalExams) { this.totalExams = totalExams; }
    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    public double getFailRate() { return failRate; }
    public void setFailRate(double failRate) { this.failRate = failRate; }
    public long getExcellentCount() { return excellentCount; }
    public void setExcellentCount(long excellentCount) { this.excellentCount = excellentCount; }
}
