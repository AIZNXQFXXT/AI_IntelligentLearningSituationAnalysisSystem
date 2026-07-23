package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourseGrade {
    @JsonProperty("courseName")
    private String courseName;
    @JsonProperty("avgScore")
    private Double avgScore;
    @JsonProperty("maxScore")
    private Double maxScore;
    @JsonProperty("minScore")
    private Double minScore;
    @JsonProperty("studentCount")
    private Integer studentCount;

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public Double getMinScore() { return minScore; }
    public void setMinScore(Double minScore) { this.minScore = minScore; }
    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
}
