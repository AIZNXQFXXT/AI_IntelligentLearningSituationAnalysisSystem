package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchoolOverview {
    @JsonProperty("totalClasses")
    private int totalClasses;
    @JsonProperty("totalTeachers")
    private int totalTeachers;
    @JsonProperty("totalStudents")
    private int totalStudents;
    @JsonProperty("totalCourses")
    private int totalCourses;
    @JsonProperty("schoolAvgScore")
    private double schoolAvgScore;
    @JsonProperty("schoolPassRate")
    private double schoolPassRate;
    @JsonProperty("failCount")
    private int failCount;

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }
    public int getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(int totalTeachers) { this.totalTeachers = totalTeachers; }
    public int getTotalStudents() { return totalStudents; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public int getTotalCourses() { return totalCourses; }
    public void setTotalCourses(int totalCourses) { this.totalCourses = totalCourses; }
    public double getSchoolAvgScore() { return schoolAvgScore; }
    public void setSchoolAvgScore(double schoolAvgScore) { this.schoolAvgScore = schoolAvgScore; }
    public double getSchoolPassRate() { return schoolPassRate; }
    public void setSchoolPassRate(double schoolPassRate) { this.schoolPassRate = schoolPassRate; }
    public int getFailCount() { return failCount; }
    public void setFailCount(int failCount) { this.failCount = failCount; }
}
