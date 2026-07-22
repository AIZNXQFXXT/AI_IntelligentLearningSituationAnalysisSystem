package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchoolStats {
    @JsonProperty("avgScore")
    private double avgScore;
    @JsonProperty("maxScore")
    private double maxScore;
    @JsonProperty("minScore")
    private double minScore;
    @JsonProperty("passRate")
    private double passRate;
    @JsonProperty("excellentCount")
    private int excellentCount;
    @JsonProperty("totalStudents")
    private int studentCount;

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }
    public double getMaxScore() { return maxScore; }
    public void setMaxScore(double maxScore) { this.maxScore = maxScore; }
    public double getMinScore() { return minScore; }
    public void setMinScore(double minScore) { this.minScore = minScore; }
    public double getPassRate() { return passRate; }
    public void setPassRate(double passRate) { this.passRate = passRate; }
    public int getExcellentCount() { return excellentCount; }
    public void setExcellentCount(int excellentCount) { this.excellentCount = excellentCount; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
}
