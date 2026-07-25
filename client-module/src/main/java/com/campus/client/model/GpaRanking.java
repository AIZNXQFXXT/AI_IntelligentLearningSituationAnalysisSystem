package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GpaRanking {
    @JsonProperty("rank")
    private Integer rank;
    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentNo")
    private String studentNo;
    @JsonProperty("studentName")
    private String studentName;
    @JsonProperty("className")
    private String className;
    @JsonProperty("gpa")
    private Double gpa;
    @JsonProperty("courseCount")
    private Integer courseCount;

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public Integer getCourseCount() { return courseCount; }
    public void setCourseCount(Integer courseCount) { this.courseCount = courseCount; }
}
