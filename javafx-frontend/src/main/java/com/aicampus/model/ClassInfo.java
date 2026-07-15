package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassInfo {
    @JsonProperty("id")
    private int id;
    @JsonProperty("grade")
    private String grade;
    @JsonProperty("className")
    private String className;
    @JsonProperty("headTeacherId")
    private int headTeacherId;
    @JsonProperty("studentCount")
    private int studentCount;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("updatedAt")
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getHeadTeacherId() { return headTeacherId; }
    public void setHeadTeacherId(int headTeacherId) { this.headTeacherId = headTeacherId; }
    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return grade + " " + className; }
}
