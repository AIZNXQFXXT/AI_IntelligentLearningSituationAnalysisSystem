package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AiComment {
    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    @JsonProperty("studentId")
    private int studentId;
    @JsonProperty("studentName")
    private String studentName;
    @JsonProperty("studentNo")
    private String studentNo;
    @JsonProperty("className")
    private String className;
    @JsonProperty("semester")
    private String semester;
    @JsonProperty("content")
    private String content;
    @JsonProperty("isTeacherEdited")
    private boolean isTeacherEdited;
    @JsonProperty("status")
    private String status;
    @JsonProperty("generatedBy")
    private String generatedBy;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("updatedAt")
    private String updatedAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isTeacherEdited() { return isTeacherEdited; }
    public void setTeacherEdited(boolean teacherEdited) { isTeacherEdited = teacherEdited; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
