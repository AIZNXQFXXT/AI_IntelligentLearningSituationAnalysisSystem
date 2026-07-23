package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GradePoint {
    @JsonProperty("studentId")
    private Long studentId;
    @JsonProperty("studentNo")
    private String studentNo;
    @JsonProperty("studentName")
    private String studentName;
    @JsonProperty("gpa")
    private Double gpa;
    @JsonProperty("courseCount")
    private Integer courseCount;

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }
    public Integer getCourseCount() { return courseCount; }
    public void setCourseCount(Integer courseCount) { this.courseCount = courseCount; }
}
